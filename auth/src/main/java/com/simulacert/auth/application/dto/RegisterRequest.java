package com.simulacert.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid")
        @Schema(description = "Email", example = "email@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(description = "Name", example = "Foo Bar", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters")
        @Schema(description = "Password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,
        @Schema(description = "Anonymous user ID", example = "1f156fcf-cf2b-61b9-9e4f-458dfe0be681")
        UUID id
) {
}

