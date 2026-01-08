package com.simulacert.auth.application.service;

import com.simulacert.auth.application.dto.AuthResponse;
import com.simulacert.auth.application.dto.ChangePasswordRequest;
import com.simulacert.auth.application.dto.LoginRequest;
import com.simulacert.auth.application.dto.RegisterRequest;
import com.simulacert.auth.application.dto.UserResponse;
import com.simulacert.auth.application.mapper.UserMapper;
import com.simulacert.auth.application.port.out.PasswordEncoderPort;
import com.simulacert.auth.application.port.out.TokenProviderPort;
import com.simulacert.auth.application.port.out.UserRepositoryPort;
import com.simulacert.auth.domain.User;
import com.simulacert.auth.domain.UserRole;
import com.simulacert.common.ClockPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private ClockPort clock;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private Instant now;
    private User testUser;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-01-04T10:00:00Z");
        testUser = User.create("test@example.com", "Test User", "hashedPassword", now);
    }

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("new@example.com", "New User", "password123", null);
        UserResponse expectedResponse = new UserResponse(
                testUser.getId(),
                "new@example.com",
                "New User",
                UserRole.USER,
                true,
                now
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(clock.now()).thenReturn(now);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(expectedResponse);

        UserResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("new@example.com");
        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "User", "password123", null);

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");

        verify(userRepository).existsByEmail(request.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfullyWithValidCredentials() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        String token = "jwt.token.here";
        AuthResponse expectedResponse = new AuthResponse(token, "Bearer ", new UserResponse(
                testUser.getId(),
                "mock", "mock", UserRole.USER, true, now
        ), null);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.password(), testUser.getPasswordHash())).thenReturn(true);
        when(tokenProvider.generateToken(testUser)).thenReturn(token);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(token);
        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder).matches(request.password(), testUser.getPasswordHash());
        verify(tokenProvider).generateToken(testUser);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        LoginRequest request = new LoginRequest("notfound@example.com", "password123");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.password(), testUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");

        verify(passwordEncoder).matches(request.password(), testUser.getPasswordHash());
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw exception when user is inactive")
    void shouldThrowExceptionWhenUserIsInactive() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        testUser.deactivate();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Account is deactivated");

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() {
        UUID userId = testUser.getId();
        UserResponse expectedResponse = new UserResponse(
                userId,
                "test@example.com",
                "Test User",
                UserRole.USER,
                true,
                now
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(expectedResponse);

        UserResponse response = authService.getUserById(userId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent user")
    void shouldThrowExceptionWhenGettingNonExistentUser() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getUserById(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: " + userId);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        UUID userId = testUser.getId();
        String oldPasswordHash = testUser.getPasswordHash();
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.currentPassword(), oldPasswordHash)).thenReturn(true);
        when(passwordEncoder.encode(request.newPassword())).thenReturn("newHashedPassword");

        authService.changePassword(userId, request);

        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(request.currentPassword(), oldPasswordHash);
        verify(passwordEncoder).encode(request.newPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when current password is incorrect")
    void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
        UUID userId = testUser.getId();
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.currentPassword(), testUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current password is incorrect");

        verify(userRepository, never()).save(any());
    }
}

