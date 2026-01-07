package com.simulacert.auth.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "oauth_states")
public class OAuthState {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected OAuthState() {
    }

    private OAuthState(UUID id, String state, AuthProvider provider, Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.state = state;
        this.provider = provider;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static OAuthState create(String state, AuthProvider provider, Instant createdAt, int expirationMinutes) {
        Objects.requireNonNull(state, "state cannot be null");
        Objects.requireNonNull(provider, "provider cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");

        Instant expiresAt = createdAt.plusSeconds(expirationMinutes * 60L);

        return new OAuthState(
                UuidCreator.getTimeOrdered(),
                state,
                provider,
                createdAt,
                expiresAt
        );
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}

