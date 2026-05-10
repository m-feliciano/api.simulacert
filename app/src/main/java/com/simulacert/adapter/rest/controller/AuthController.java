package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.controller.openapi.AuthControllerOpenApi;
import com.simulacert.auth.application.dto.AuthResponse;
import com.simulacert.auth.application.dto.ChangePasswordRequest;
import com.simulacert.auth.application.dto.LoginRequest;
import com.simulacert.auth.application.dto.RefreshTokenRequest;
import com.simulacert.auth.application.dto.RegisterRequest;
import com.simulacert.auth.application.dto.UserResponse;
import com.simulacert.auth.application.port.in.AuthUseCase;
import com.simulacert.util.UserContextHolder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
        UserResponse response = authUseCase.register(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/users/{userId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authUseCase.login(request));
    }

    @Override
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(authUseCase.getUserById(userId));
    }

    @GetMapping("/users/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(authUseCase.getUserByEmail(email));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(authUseCase.getUsers());
    }

    @PutMapping("/users/{userId}/password")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authUseCase.changePassword(userId, request);
    }

    @PutMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateUser(@PathVariable UUID userId) {
        authUseCase.activateUser(userId);
    }

    @Override
    @PutMapping("/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUser(@PathVariable UUID userId) {
        authUseCase.deactivateUser(userId);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getCurrentUser() {
        return authUseCase.getUserById(UserContextHolder.getUser());
    }

    @PostMapping("/users/anonymous")
    public ResponseEntity<AuthResponse> createAnonymousUser() {
        UserResponse anonymous = authUseCase.createAnonymousUser();

        Counter.builder("events.policy.executed")
                .tag("policy", "auth.anonymous_users.created")
                .register(meterRegistry)
                .increment();

        AuthResponse response = authUseCase.loginAnonymous(anonymous.id(), anonymous.dummyPassword());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest req) {
        AuthResponse response = authUseCase.refreshToken(req.refreshToken());
        return ResponseEntity.ok(response);
    }
}

