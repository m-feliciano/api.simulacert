package br.com.simulaaws.auth.application.port.in;

import br.com.simulaaws.auth.application.dto.AuthResponse;
import br.com.simulaaws.auth.application.dto.ChangePasswordRequest;
import br.com.simulaaws.auth.application.dto.LoginRequest;
import br.com.simulaaws.auth.application.dto.RegisterRequest;
import br.com.simulaaws.auth.application.dto.UserResponse;

import java.util.UUID;

public interface AuthUseCase {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getUserById(UUID userId);

    UserResponse getUserByEmail(String email);

    void changePassword(UUID userId, ChangePasswordRequest request);

    void deactivateUser(UUID userId);

    void activateUser(UUID userId);
}

