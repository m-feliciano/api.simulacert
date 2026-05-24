package com.simulacert.auth.application.dto;

import com.simulacert.auth.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        @Schema(description = "User ID", example = "1f156fcf-cf2b-61b9-9e4f-458dfe0be681") UUID id,
        @Schema(description = "Email", example = "email@email.com") String email,
        @Schema(description = "Name", example = "Foo Bar") String name,
        @Schema(description = "Role", example = "USER") UserRole role,
        @Schema(description = "Active", example = "true") boolean active,
        @Schema(description = "Created At", example = "2026-05-23T20:13:58Z") Instant createdAt,
        @Schema(description = "Dummy Password", example = "dummy-password") String dummyPassword,
        @Schema(description = "Supporter", example = "false") Boolean supporter,
        @Schema(description = "Type", example = "AUTHENTICATED") String type) {

    public UserResponse(UUID id,
                        String email,
                        String name,
                        UserRole role,
                        boolean active,
                        Instant createdAt) {
        this(id, email, name, role, active, createdAt, null, null, null);
    }
}

