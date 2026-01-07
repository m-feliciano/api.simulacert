package com.simulacert.auth.application.port.out;

import com.simulacert.auth.domain.AuthProvider;
import com.simulacert.auth.domain.OAuthState;

import java.time.Instant;
import java.util.Optional;

public interface OAuthStateRepositoryPort {

    OAuthState save(OAuthState state);

    Optional<OAuthState> findByStateAndProvider(String state, AuthProvider provider);

    void deleteExpired(Instant now);
}

