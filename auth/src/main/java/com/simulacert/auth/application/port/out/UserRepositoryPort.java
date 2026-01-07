package com.simulacert.auth.application.port.out;

import com.simulacert.auth.domain.AuthProvider;
import com.simulacert.auth.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    List<User> findAll();
}


