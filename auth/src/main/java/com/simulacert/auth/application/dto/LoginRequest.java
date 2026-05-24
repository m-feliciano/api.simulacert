package com.simulacert.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Email", example = "email@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @Email(message = "Email must be valid")
        String email,

        @Schema(description = "Password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        String password
) {
}
