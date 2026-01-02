package br.com.simulaaws.auth.application.port.out;

import br.com.simulaaws.auth.domain.User;

public interface TokenProviderPort {

    String generateToken(User user);

    String extractUserId(String token);

    boolean validateToken(String token);
}

