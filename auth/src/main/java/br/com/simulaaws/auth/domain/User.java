package br.com.simulaaws.auth.domain;

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

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

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

    public static User create(String email, String name, String passwordHash, Instant createdAt) {
        Objects.requireNonNull(email, "email cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(passwordHash, "passwordHash cannot be null");

        return User.builder()
                .id(UUID.randomUUID())
                .email(email.toLowerCase().trim())
                .name(name.trim())
                .passwordHash(passwordHash)
                .role(UserRole.USER)
                .active(true)
                .createdAt(createdAt)
                .build();
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
}

