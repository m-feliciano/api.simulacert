package br.com.simulaaws.auth.application.dto;

public record AuthResponse(
        String token,
        String type,
        UserResponse user
) {
    public static AuthResponse of(String token, UserResponse user) {
        return new AuthResponse(token, "Bearer", user);
    }
}

