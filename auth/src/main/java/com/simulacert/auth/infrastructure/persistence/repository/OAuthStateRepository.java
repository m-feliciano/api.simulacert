package com.simulacert.auth.infrastructure.persistence.repository;

import com.simulacert.auth.domain.AuthProvider;
import com.simulacert.auth.domain.OAuthState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthStateRepository extends JpaRepository<OAuthState, UUID> {

    Optional<OAuthState> findByStateAndProvider(String state, AuthProvider provider);

    @Modifying
    @Query("DELETE FROM OAuthState o WHERE o.expiresAt < :now")
    void deleteExpired(@Param("now") Instant now);
}

