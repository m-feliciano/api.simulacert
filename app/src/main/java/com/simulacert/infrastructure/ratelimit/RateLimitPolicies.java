package com.simulacert.infrastructure.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitPolicies {

    /**
     * There's a nginx rate limit in front of the application as well:
     * burst=3 for auth endpoints, burst=10 for overall traffic
     * <p>
     * Default policy: 100 requests per minute (for authenticated API calls)
     * Unknown policy: 40 requests per minute (for non-API paths - bots, scanners, etc.)
     * Auth policy: 10 requests per 30 seconds (login, register, oauth)
     * Expensive operations policy: 5 requests per hour (anonymous user creation)
     * LLM operations policy: 10 requests per 2 hours (AI explanations)
     */
    private PolicyConfig defaultConfig = new PolicyConfig(100, 100, Duration.ofMinutes(1));
    private PolicyConfig unknown = new PolicyConfig(40, 40, Duration.ofMinutes(1));
    private PolicyConfig auth = new PolicyConfig(10, 10, Duration.ofSeconds(30));
    private PolicyConfig expensive = new PolicyConfig(5, 5, Duration.ofHours(1));
    private PolicyConfig llm = new PolicyConfig(10, 10, Duration.ofHours(2));

    public RateLimitPolicy defaultPolicy() {
        return RateLimitPolicy.of("default", defaultConfig.capacity, defaultConfig.refillTokens, defaultConfig.refillPeriod);
    }

    public RateLimitPolicy unknown() {
        return RateLimitPolicy.of("unknown", unknown.capacity, unknown.refillTokens, unknown.refillPeriod);
    }

    public RateLimitPolicy auth() {
        return RateLimitPolicy.of("auth", auth.capacity, auth.refillTokens, auth.refillPeriod);
    }

    public RateLimitPolicy expensive() {
        return RateLimitPolicy.of("expensive", expensive.capacity, expensive.refillTokens, expensive.refillPeriod);
    }

    public RateLimitPolicy llm() {
        return RateLimitPolicy.of("llm", llm.capacity, llm.refillTokens, llm.refillPeriod);
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolicyConfig {
        private int capacity;
        private int refillTokens;
        private Duration refillPeriod;
    }
}

