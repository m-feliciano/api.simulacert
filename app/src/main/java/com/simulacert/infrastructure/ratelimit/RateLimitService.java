package com.simulacert.infrastructure.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private final Cache<String, TokenBucket> buckets;

    public RateLimitService() {
        this.buckets = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(50_000)
                .build();
    }

    public boolean allow(String key, RateLimitPolicy policy) {
        String cacheKey = key + ":" + policy.name();
        TokenBucket bucket = buckets.get(cacheKey, k -> createBucket(policy));
        return bucket.tryConsume();
    }

    private TokenBucket createBucket(RateLimitPolicy policy) {
        return new TokenBucket(
                policy.capacity(),
                policy.refillTokens(),
                policy.refillPeriod()
        );
    }

    public TokenBucket getBucket(String key) {
        return buckets.getIfPresent(key);
    }
}

