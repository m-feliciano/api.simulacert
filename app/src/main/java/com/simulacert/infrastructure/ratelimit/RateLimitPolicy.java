package com.simulacert.infrastructure.ratelimit;

import java.time.Duration;

public record RateLimitPolicy(
        String name,
        int capacity,
        int refillTokens,
        Duration refillPeriod
) {

    public static RateLimitPolicy of(String name, int capacity, int refillTokens, Duration refillPeriod) {
        return new RateLimitPolicy(name, capacity, refillTokens, refillPeriod);
    }
}

