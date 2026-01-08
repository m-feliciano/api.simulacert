package com.simulacert.auth.infrastructure.adapter;

import com.simulacert.auth.application.port.out.TokenProviderPort;
import com.simulacert.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private final Key key;
    private final long jwtExpirationMs;

    public JwtTokenProviderAdapter(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration:86400000}") long jwtExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("type", "ACCESS")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        Date now = new Date();
        long days = Duration.ofDays(7).toMillis();
        Date expiryDate = new Date(now.getTime() + days);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("type", "REFRESH")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public String extractUserId(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    @Override
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);

            if (!"ACCESS".equals(claims.get("type", String.class))) {
                throw new IllegalArgumentException("Token is not an access token");
            }
            return true;
        } catch (Exception ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public UUID extractUserIdRefreshToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken);

        if (!"REFRESH".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Not a refresh token");
        }

        return UUID.fromString(claims.getSubject());
    }

    private String plainToken(String refreshToken) {
        if (refreshToken.startsWith("Bearer ")) {
            return refreshToken.substring(7);
        }
        return refreshToken;
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(plainToken(token))
                .getBody();
    }

}

