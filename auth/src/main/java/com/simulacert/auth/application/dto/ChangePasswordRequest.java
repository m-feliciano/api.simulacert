package com.simulacert.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @Schema(description = "Current Password", example = "current123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "New Password", example = "new123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        String newPassword
) {
}

