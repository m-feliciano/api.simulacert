package com.simulacert.auth.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private UUID id;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private long version;

    @Column(name = "supporter")
    private Boolean supporter;

    @Column(name = "type")
    private String type = "AUTHENTICATED"; // or anonymous

    public static User create(String email, String name, String passwordHash, Instant createdAt) {
        Objects.requireNonNull(email, "email cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(passwordHash, "passwordHash cannot be null");

        return User.builder()
                .id(UuidCreator.getTimeOrdered())
                .email(email.toLowerCase().trim())
                .name(name.trim())
                .passwordHash(passwordHash)
                .role(UserRole.USER)
                .type("AUTHENTICATED")
                .provider(AuthProvider.LOCAL)
                .active(true)
                .createdAt(createdAt)
                .build();
    }

    public static User createAnon(String passwordHash) {
        String hash = UUID.randomUUID().toString().substring(10).replace("-", "");

        return User.builder()
                .id(UuidCreator.getTimeOrdered())
                .role(UserRole.USER)
                .email("anon_" + hash + "@example.com")
                .passwordHash(passwordHash)
                .provider(AuthProvider.LOCAL)
                .active(true)
                .type("ANONYMOUS")
                .createdAt(Instant.now())
                .build();
    }

    public static User createFromOAuth(String email, String name, AuthProvider provider, String providerId, Instant createdAt) {
        Objects.requireNonNull(email, "email cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(provider, "provider cannot be null");
        Objects.requireNonNull(providerId, "providerId cannot be null");

        return User.builder()
                .id(UuidCreator.getTimeOrdered())
                .email(email.toLowerCase().trim())
                .name(name.trim())
                .provider(provider)
                .providerId(providerId)
                .role(UserRole.USER)
                .active(true)
                .createdAt(createdAt)
                .build();
    }

    public void register(String email, String name, String passwordHash, Instant updatedAt) {
        this.email = email.toLowerCase().trim();
        this.name = name.trim();
        this.passwordHash = passwordHash;
        this.type = "AUTHENTICATED";
        this.updatedAt = updatedAt;
    }

    public void updatePassword(String newPasswordHash) {
        Objects.requireNonNull(newPasswordHash, "passwordHash cannot be null");
        this.passwordHash = newPasswordHash;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void updateProfile(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isAnonymous() {
        return "ANONYMOUS".equalsIgnoreCase(this.type);
    }
}

