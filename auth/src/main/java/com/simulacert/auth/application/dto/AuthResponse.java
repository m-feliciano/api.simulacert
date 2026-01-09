package com.simulacert.auth.application.dto;

public record AuthResponse(
        String token,
        String type,
        UserResponse user,
        String refreshToken
) {
    public static AuthResponse of(String token, UserResponse user, String refreshToken) {
        return new AuthResponse(token, "Bearer", user, refreshToken);
    }

    public static AuthResponse of(String token, UserResponse user) {
        return new AuthResponse(token, "Bearer", user, null);
    }
}

