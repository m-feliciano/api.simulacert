package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.controller.openapi.AuthControllerOpenApi;
import com.simulacert.adapter.rest.dto.RefreshTokenRequest;
import com.simulacert.auth.application.dto.AuthResponse;
import com.simulacert.auth.application.dto.ChangePasswordRequest;
import com.simulacert.auth.application.dto.LoginRequest;
import com.simulacert.auth.application.dto.RegisterRequest;
import com.simulacert.auth.application.dto.UserResponse;
import com.simulacert.auth.application.port.in.AuthUseCase;
import com.simulacert.auth.domain.User;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerOpenApi {

    private final AuthUseCase authUseCase;
    private final MeterRegistry meterRegistry;

    @Override
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for email: {}", request.email());

        UserResponse response = authUseCase.register(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/users/{userId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.email());

        AuthResponse response = authUseCase.login(request);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.debug("Get user by id: {}", userId);

        UserResponse response = authUseCase.getUserById(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.debug("Get user by email: {}", email);

        UserResponse response = authUseCase.getUserByEmail(email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsers() {
        log.debug("Get all users");

        List<UserResponse> response = authUseCase.getUsers();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change dummyPassword request for user: {}", userId);

        authUseCase.changePassword(userId, request);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(@PathVariable UUID userId) {
        log.info("Activate user request: {}", userId);

        authUseCase.activateUser(userId);

        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        log.info("Deactivate user request: {}", userId);

        authUseCase.deactivateUser(userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.debug("Get current authenticated user");

        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        UserResponse response = authUseCase.getUserById(user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/anonymous")
    public ResponseEntity<AuthResponse> createAnonymousUser() {
        log.debug("Create anonymous user request");

        UserResponse anonymous = authUseCase.createAnonymousUser();

        Counter.builder("events.policy.executed")
                .tag("policy", "auth.anonymous_users.created")
                .register(meterRegistry)
                .increment();

        AuthResponse response = authUseCase.loginAnonymous(anonymous.id(), anonymous.dummyPassword());

        return ResponseEntity.ok(response);
    }

    // refresh-token
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest req) {
        log.info("Refresh token request");

        AuthResponse response = authUseCase.refreshToken(req.refreshToken());
        return ResponseEntity.ok(response);
    }
}

