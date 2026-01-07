package com.simulacert.auth.infrastructure.persistence.adapter;

import com.simulacert.auth.application.port.out.UserRepositoryPort;
import com.simulacert.auth.domain.AuthProvider;
import com.simulacert.auth.domain.User;
import com.simulacert.auth.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository repository;

    @Override
    public User save(User user) {
        return repository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return repository.findByProviderAndProviderId(provider, providerId);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }
}

