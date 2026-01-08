package com.simulacert.auth.application.port.out;

import com.simulacert.auth.domain.User;

import java.util.UUID;

public interface TokenProviderPort {

    String generateToken(User user);

    String generateRefreshToken(User user);

    String extractUserId(String token);

    boolean validateToken(String token);

    UUID validateAndExtractUserId(String refreshToken);
}

