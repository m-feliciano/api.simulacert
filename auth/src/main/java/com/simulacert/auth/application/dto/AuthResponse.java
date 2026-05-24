package com.simulacert.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "Access Token", example = "eyJhbGciOi...") String token,
        @Schema(description = "Token Type", example = "Bearer") String type,
        UserResponse user,
        @Schema(description = "Refresh Token", example = "refresh-token-example") String refreshToken
) {
    public static AuthResponse of(String token, UserResponse user, String refreshToken) {
        return new AuthResponse(token, "Bearer", user, refreshToken);
    }

    public static AuthResponse of(String token, UserResponse user) {
        return new AuthResponse(token, "Bearer", user, null);
    }
}

