package com.simulacert.auth.application.dto;

import com.simulacert.auth.domain.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name,
        UserRole role,
        boolean active,
        Instant createdAt
) {
}

