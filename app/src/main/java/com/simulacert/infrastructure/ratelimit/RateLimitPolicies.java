package com.simulacert.infrastructure.ratelimit;

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

    private PolicyConfig defaultConfig = new PolicyConfig(100, 100, Duration.ofSeconds(60));
    private PolicyConfig anonymous = new PolicyConfig(20, 20, Duration.ofSeconds(60));
    private PolicyConfig auth = new PolicyConfig(10, 10, Duration.ofSeconds(60));
    private PolicyConfig expensive = new PolicyConfig(5, 5, Duration.ofHours(1));
    private PolicyConfig llm = new PolicyConfig(10, 10, Duration.ofMinutes(2));

    public RateLimitPolicy defaultPolicy() {
        return RateLimitPolicy.of("default", defaultConfig.capacity, defaultConfig.refillTokens, defaultConfig.refillPeriod);
    }

    public RateLimitPolicy anonymous() {
        return RateLimitPolicy.of("anonymous", anonymous.capacity, anonymous.refillTokens, anonymous.refillPeriod);
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
    public static class PolicyConfig {
        private int capacity;
        private int refillTokens;
        private Duration refillPeriod;

        public PolicyConfig(int capacity, int refillTokens, Duration refillPeriod) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillPeriod = refillPeriod;
        }
    }
}

