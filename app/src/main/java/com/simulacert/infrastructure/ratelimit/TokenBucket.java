package com.simulacert.infrastructure.ratelimit;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public final class TokenBucket {

    private final int capacity;
    private final int refillTokens;
    private final Duration refillPeriod;

    private final AtomicInteger tokens;
    private Instant lastRefill;

    public TokenBucket(int capacity, int refillTokens, Duration refillPeriod) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillPeriod = refillPeriod;
        this.tokens = new AtomicInteger(capacity);
        this.lastRefill = Instant.now();
    }

    public boolean tryConsume() {
        refillIfNeeded();
        if (tokens.get() <= 0) return false;

        tokens.decrementAndGet();
        return true;
    }

    private void refillIfNeeded() {
        Instant now = Instant.now();
        long secondsSinceLastRefill = Duration.between(lastRefill, now).getSeconds();
        long periodSeconds = refillPeriod.getSeconds();

        if (secondsSinceLastRefill >= periodSeconds) {
            long periodsElapsed = secondsSinceLastRefill / periodSeconds;
            int tokensToAdd = (int) (periodsElapsed * refillTokens);
            tokens.updateAndGet(current -> Math.min(capacity, current + tokensToAdd));
            lastRefill = lastRefill.plusSeconds(periodsElapsed * periodSeconds);
        }
    }
}

