package br.com.simulaaws.adapter.rest.controller;

import br.com.simulaaws.auth.application.dto.AuthResponse;
import br.com.simulaaws.auth.application.dto.ChangePasswordRequest;
import br.com.simulaaws.auth.application.dto.LoginRequest;
import br.com.simulaaws.auth.application.dto.RegisterRequest;
import br.com.simulaaws.auth.application.dto.UserResponse;
import br.com.simulaaws.auth.application.port.in.AuthUseCase;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for email: {}", request.email());

        AuthResponse response = authUseCase.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.email());

        AuthResponse response = authUseCase.login(request);

        return ResponseEntity.ok(response);
    }

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

    @PutMapping("/users/{userId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request for user: {}", userId);

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

    @PutMapping("/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        log.info("Deactivate user request: {}", userId);

        authUseCase.deactivateUser(userId);

        return ResponseEntity.noContent().build();
    }
}

