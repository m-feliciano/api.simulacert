package com.simulacert.auth.infrastructure.persistence.adapter;

import com.simulacert.auth.application.port.out.OAuthStateRepositoryPort;
import com.simulacert.auth.domain.AuthProvider;
import com.simulacert.auth.domain.OAuthState;
import com.simulacert.auth.infrastructure.persistence.repository.OAuthStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OAuthStateRepositoryAdapter implements OAuthStateRepositoryPort {

    private final OAuthStateRepository repository;

    @Override
    public OAuthState save(OAuthState state) {
        return repository.save(state);
    }

    @Override
    public Optional<OAuthState> findByStateAndProvider(String state, AuthProvider provider) {
        return repository.findByStateAndProvider(state, provider);
    }

    @Override
    @Transactional
    public void deleteExpired(Instant now) {
        repository.deleteExpired(now);
    }
}

