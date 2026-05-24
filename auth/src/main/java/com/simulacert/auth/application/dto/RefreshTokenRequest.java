package com.simulacert.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshTokenRequest(
        @Schema(description = "Refresh Token", example = "ey383m...", requiredMode = Schema.RequiredMode.REQUIRED) String refreshToken) {
}
