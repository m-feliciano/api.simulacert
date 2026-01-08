package com.simulacert.auth.application.port.out;

import com.simulacert.auth.domain.User;

import java.util.UUID;

public interface TokenProviderPort {

    String generateToken(User user);

    String generateRefreshToken(User user);

    String extractUserId(String token);

    boolean validateAccessToken(String token);

    UUID extractUserIdRefreshToken(String refreshToken);
}

