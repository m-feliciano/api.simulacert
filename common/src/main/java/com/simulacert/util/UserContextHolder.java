package com.simulacert.util;

import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class UserContextHolder {

    private static final ThreadLocal<UUID> CTX = new ThreadLocal<>();

    public static void clear() {
        CTX.remove();
    }

    public static UUID getUser() {
        return CTX.get();
    }

    public static void setUser(UUID userId) {
        CTX.set(userId);
    }
}