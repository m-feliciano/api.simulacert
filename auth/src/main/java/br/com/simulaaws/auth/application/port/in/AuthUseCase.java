package br.com.simulaaws.auth.application.port.in;

import br.com.simulaaws.auth.application.dto.AuthResponse;
import br.com.simulaaws.auth.application.dto.ChangePasswordRequest;
import br.com.simulaaws.auth.application.dto.LoginRequest;
import br.com.simulaaws.auth.application.dto.RegisterRequest;
import br.com.simulaaws.auth.application.dto.UserResponse;

import java.util.UUID;

public interface AuthUseCase {

    /**
     * Register a new user
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate user and generate JWT token
     */
    AuthResponse login(LoginRequest request);

    /**
     * Get user by ID
     */
    UserResponse getUserById(UUID userId);

    /**
     * Get user by email
     */
    UserResponse getUserByEmail(String email);

    /**
     * Change user password
     */
    void changePassword(UUID userId, ChangePasswordRequest request);

    /**
     * Deactivate user account
     */
    void deactivateUser(UUID userId);

    /**
     * Activate user account
     */
    void activateUser(UUID userId);
}

