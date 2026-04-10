package com.simulacert.config.security.dev;

import com.simulacert.config.security.JwtAuthenticationFilter;
import com.simulacert.infrastructure.ratelimit.RateLimitFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Slf4j
@Configuration
@Profile({"dev", "default"})
public class SecurityConfigDev {

    @Bean
    public SecurityFilterChain securityFilterChainDev(HttpSecurity http,
                                                      CorsConfigurationSource corsConfigurationSource,
                                                      RateLimitFilter rateLimitFilter,
                                                      JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        log.info("SecurityFilterChain for DEV profile");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException)
                                -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((request, response, accessDeniedException)
                                -> response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/password-reset/**",
                                "/api/v1/auth/refresh-token",
                                "/api/v1/auth/oauth/**",
                                "/api/v1/auth/users/anonymous"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/exams", "/api/v1/exams/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RateLimitFilter.class);

        return http.build();
    }
}

