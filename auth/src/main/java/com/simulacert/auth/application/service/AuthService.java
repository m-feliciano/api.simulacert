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

import java.util.Base64;
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

        if (request.id() != null) {
            log.debug("User already registered for {}", request.id());
            return updateAnonymousUser(request);
        }

        log.debug("New user registration for email: {}", request.email());
        return registerUser(request);
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
        String refreshToken = tokenProvider.generateRefreshToken(user);

        UserResponse userResponse = userMapper.toResponse(user);
        return AuthResponse.of(token, userResponse, refreshToken);
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

    @Override
    @Transactional
    public UserResponse createAnonymousUser() {
        log.info("Creating anonymous user");

        String hash = UUID.randomUUID().toString().substring(0, 12).replace("-", "");
        String dummyPassword = Base64.getEncoder().encodeToString(hash.getBytes());
        User user = User.createAnon(dummyPassword);
        user = userRepository.save(user);

        log.info("Anonymous user created with id: {}", user.getId());

        return userMapper.toResponseWithPass(user, dummyPassword);
    }

    @Override
    public AuthResponse loginAnonymous(UUID anonymousUserId, String dummyPassword) {
        log.info("Anonymous login attempt for user id: {}", anonymousUserId);

        User user = userRepository.findById(anonymousUserId)
                .orElseThrow(() -> {
                    log.warn("Anonymous user not found with id: {}", anonymousUserId);
                    return new IllegalArgumentException("Anonymous user not found");
                });

        if (!user.isAnonymous() || !user.isActive()) {
            log.warn("User with id {} is not an active anonymous user", anonymousUserId);
            throw new IllegalArgumentException("Invalid anonymous user credentials");
        }

        if (dummyPassword == null || dummyPassword.isEmpty()) {
            log.warn("Dummy  is null for anonymous user id: {}", anonymousUserId);
            throw new IllegalArgumentException("Invalid anonymous user credentials");
        }

        if (!dummyPassword.equals(user.getPasswordHash())) {
            log.warn("Invalid  for anonymous user id: {}", anonymousUserId);
            throw new IllegalArgumentException("Invalid anonymous user credentials");
        }

        String token = tokenProvider.generateToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        UserResponse userResponse = userMapper.toResponseWithPass(user, dummyPassword);
        return AuthResponse.of(token, userResponse, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Refresh token is null or empty");
            throw new IllegalArgumentException("Invalid refresh token");
        }

        UUID userId = tokenProvider.extractUserIdRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for refresh token with user id: {}", userId);
                    return new IllegalArgumentException("Invalid refresh token");
                });
        if (!user.isActive()) {
            log.warn("User account is deactivated for user id: {}", userId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        String newToken = tokenProvider.generateToken(user);
        String newRefreshToken = tokenProvider.generateRefreshToken(user);

        UserResponse userResponse = userMapper.toResponse(user);
        return AuthResponse.of(newToken, userResponse, newRefreshToken);
    }

    private UserResponse registerUser(RegisterRequest request) {
        String name = request.name();
        if (name == null || name.isEmpty()) {
            name = request.email().split("@")[0];
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = User.create(request.email(), name, passwordHash, clock.now());

        user = userRepository.save(user);
        log.info("User registered successfully with id: {}", user.getId());

        return userMapper.toResponse(user);
    }

    private UserResponse updateAnonymousUser(RegisterRequest request) {
        User anonUser = userRepository.findById(request.id())
                .orElseThrow(() -> {
                    log.warn("Anonymous user not found with id: {}", request.id());
                    return new IllegalArgumentException("Anonymous user not found");
                });

        if (!anonUser.isAnonymous()) {
            log.warn("User with id {} is not anonymous", request.id());
            throw new IllegalArgumentException("User ID is not associated with an anonymous user");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        String name = request.name() != null && !request.name().isEmpty() ? request.name() : request.email().split("@")[0];

        anonUser.register(request.email(), name, passwordHash, clock.now());
        anonUser = userRepository.save(anonUser);
        log.info("Anonymous user registered successfully with id: {}", anonUser.getId());

        return userMapper.toResponse(anonUser);
    }
}

