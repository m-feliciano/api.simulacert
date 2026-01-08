package com.simulacert.infrastructure.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

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

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        if (shouldSkipRateLimit(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitPolicy policy = resolvePolicy(path);
        String key = keyResolver.resolve(request);

        if (!rateLimitService.allow(key, policy)) {
            recordBlocked(policy);
            write429Response(response, policy);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipRateLimit(String path) {
        return path.startsWith("/actuator/health");
    }

    private RateLimitPolicy resolvePolicy(String path) {
        if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/register")) {
            return policies.auth();
        }

        if (path.startsWith("/api/v1/auth/oauth")) {
            return policies.auth();
        }


        if (path.startsWith("/questions/") && path.contains("/explanations")) {
            return policies.llm();
        }

        if (path.startsWith("/api/v1/auth/users/anonymous")) {
            return policies.expensive();
        }

        if (path.startsWith("/api")) {
            return policies.defaultPolicy();
        }

        return policies.anonymous();
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

