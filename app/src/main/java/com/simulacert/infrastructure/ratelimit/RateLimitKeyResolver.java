package com.simulacert.infrastructure.ratelimit;

import com.simulacert.auth.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class RateLimitKeyResolver {

    public String resolve(HttpServletRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof UsernamePasswordAuthenticationToken
            && authentication.getPrincipal() instanceof User user) {
            return "user:" + user.getId().toString();
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            String firstIp = forwardedFor.split(",")[0].trim();
            return "ip:" + firstIp;
        }

        return "ip:" + request.getRemoteAddr();
    }
}

