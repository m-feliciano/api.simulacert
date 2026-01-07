package com.simulacert.infrastructure.ratelimit;

import java.time.Duration;
import java.time.Instant;

public final class TokenBucket {

    private final int capacity;
    private final int refillTokens;
    private final Duration refillPeriod;

    private int tokens;
    private Instant lastRefill;

    public TokenBucket(int capacity, int refillTokens, Duration refillPeriod) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillPeriod = refillPeriod;
        this.tokens = capacity;
        this.lastRefill = Instant.now();
    }

    public synchronized boolean tryConsume() {
        refillIfNeeded();
        if (tokens <= 0) return false;

        tokens--;
        return true;
    }

    private void refillIfNeeded() {
        Instant now = Instant.now();
        long secondsSinceLastRefill = Duration.between(lastRefill, now).getSeconds();
        long periodSeconds = refillPeriod.getSeconds();

        if (secondsSinceLastRefill >= periodSeconds) {
            long periodsElapsed = secondsSinceLastRefill / periodSeconds;
            int tokensToAdd = (int) (periodsElapsed * refillTokens);
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefill = lastRefill.plusSeconds(periodsElapsed * periodSeconds);
        }
    }
}

