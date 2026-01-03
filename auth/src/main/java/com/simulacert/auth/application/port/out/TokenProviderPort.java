package com.simulacert.auth.application.port.out;

import com.simulacert.auth.domain.User;

public interface TokenProviderPort {

    String generateToken(User user);

    String extractUserId(String token);

    boolean validateToken(String token);
}

