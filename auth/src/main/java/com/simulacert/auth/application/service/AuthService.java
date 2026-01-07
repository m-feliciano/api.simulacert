package com.simulacert.auth.application.service;

import com.simulacert.auth.application.dto.AuthResponse;
import com.simulacert.auth.application.dto.ChangePasswordRequest;
import com.simulacert.auth.application.dto.LoginRequest;
import com.simulacert.auth.application.dto.RegisterRequest;
import com.simulacert.auth.application.dto.UserResponse;
import com.simulacert.auth.application.mapper.UserMapper;
import com.simulacert.auth.application.port.in.AuthUseCase;
import com.simulacert.auth.application.port.out.PasswordEncoderPort;
import com.simulacert.auth.application.port.out.TokenProviderPort;
import com.simulacert.auth.application.port.out.UserRepositoryPort;
import com.simulacert.auth.domain.User;
import com.simulacert.common.ClockPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;
    private final ClockPort clock;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse register(@Valid RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Email already exists: {}", request.email());
            throw new IllegalArgumentException("Email already registered");
        }

        String name = request.name();
        if (name == null || name.isEmpty()) {
            name = request.email().split("@")[0];
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = User.create(
                request.email(),
                name,
                passwordHash,
                clock.now()
        );

        user = userRepository.save(user);
        log.info("User registered successfully with id: {}", user.getId());

        return userMapper.toResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", request.email());
                    return new IllegalArgumentException("Invalid email or password");
                });

        if (!user.isActive()) {
            log.warn("User account is deactivated: {}", request.email());
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (user.getProvider() != com.simulacert.auth.domain.AuthProvider.LOCAL) {
            log.warn("User registered with OAuth provider attempted password login: {}", request.email());
            throw new IllegalArgumentException("This account uses " + user.getProvider() + " authentication");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Invalid password for email: {}", request.email());
            throw new IllegalArgumentException("Invalid email or password");
        }

        log.info("User logged in successfully: {}", user.getId());

        String token = tokenProvider.generateToken(user);

        UserResponse userResponse = userMapper.toResponse(user);
        return AuthResponse.of(token, userResponse);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        log.debug("Getting user by id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getProvider() != com.simulacert.auth.domain.AuthProvider.LOCAL) {
            log.warn("OAuth user attempted to change password: {}", userId);
            throw new IllegalArgumentException("Cannot change password for OAuth accounts");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            log.warn("Current password is invalid for user: {}", userId);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        String newPasswordHash = passwordEncoder.encode(request.newPassword());

        user.updatePassword(newPasswordHash);
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        log.info("Deactivating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.deactivate();
        userRepository.save(user);

        log.info("User deactivated successfully: {}", userId);
    }

    @Override
    @Transactional
    public void activateUser(UUID userId) {
        log.info("Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.activate();
        userRepository.save(user);

        log.info("User activated successfully: {}", userId);
    }

    @Override
    public List<UserResponse> getUsers() {
        log.debug("Getting all users");

        List<User> users = userRepository.findAll();

        return userMapper.toResponseList(users);
    }
}

