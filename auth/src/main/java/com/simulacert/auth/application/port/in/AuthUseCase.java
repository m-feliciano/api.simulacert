package com.simulacert.auth.application.port.in;

import com.simulacert.auth.application.dto.AuthResponse;
import com.simulacert.auth.application.dto.ChangePasswordRequest;
import com.simulacert.auth.application.dto.LoginRequest;
import com.simulacert.auth.application.dto.RegisterRequest;
import com.simulacert.auth.application.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface AuthUseCase {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getUserById(UUID userId);

    UserResponse getUserByEmail(String email);

    void changePassword(UUID userId, ChangePasswordRequest request);

    void deactivateUser(UUID userId);

    void activateUser(UUID userId);

    List<UserResponse> getUsers();

    UserResponse createAnonymousUser();

    AuthResponse loginAnonymous(UUID anonymousUserId, String dummyPassword);

    AuthResponse refreshToken(String refreshToken);
}

