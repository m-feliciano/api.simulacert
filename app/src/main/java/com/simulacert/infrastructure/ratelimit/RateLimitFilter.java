package com.simulacert.infrastructure.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitKeyResolver keyResolver;
    private final RateLimitPolicies policies;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public RateLimitFilter(
            RateLimitService rateLimitService,
            RateLimitKeyResolver keyResolver,
            RateLimitPolicies policies,
            MeterRegistry meterRegistry,
            ObjectMapper objectMapper,
            @Value("${app.rate-limit.enabled:true}") boolean enabled) {
        this.rateLimitService = rateLimitService;
        this.keyResolver = keyResolver;
        this.policies = policies;
        this.meterRegistry = meterRegistry;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (enabled && !shouldSkipRateLimit(path)) {
            RateLimitPolicy policy = resolvePolicy(path);
            String key = keyResolver.resolve(request);

            if (!rateLimitService.allow(key, policy)) {
                log.warn("Rate limit exceeded: key={}, policy={}, capacity={}, path={}", key, policy.name(), policy.capacity(), path);
                recordBlocked(policy);
                write429Response(response, policy);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipRateLimit(String path) {
        return path.startsWith("/actuator/health");
    }

    private RateLimitPolicy resolvePolicy(String path) {
        // Auth endpoints - most restrictive
        if (path.contains("/auth/login")
            || path.contains("/auth/register")
            || path.contains("/auth/oauth")) {
            return policies.auth();
        }

        // Expensive operations
        if (path.contains("/users/anonymous")) {
            return policies.expensive();
        }

        if (path.contains("/questions/explanations")) {
            return policies.defaultPolicy();
        }

        // LLM operations
        if (path.contains("/questions/") && path.contains("/explanations")) {
            return policies.llm();
        }

        // All API endpoints - default policy
        if (path.startsWith("/api/")) {
            return policies.defaultPolicy();
        }

        // Static resources, health checks, or bot scans - unknown policy
        return policies.unknown();
    }

    private void write429Response(HttpServletResponse response, RateLimitPolicy policy) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(policy.refillPeriod().getSeconds()));

        Map<String, String> body = Map.of("error", "rate_limit_exceeded");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void recordBlocked(RateLimitPolicy policy) {
        Counter.builder("rate_limit.blocked")
                .tag("policy", policy.name())
                .register(meterRegistry)
                .increment();
    }
}

