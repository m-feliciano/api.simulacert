package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.dto.OAuthExchangeRequest;
import com.simulacert.auth.application.dto.AuthResponse;
import com.simulacert.auth.application.port.in.OAuthUseCase;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth/oauth")
public class OAuthController {

    private final OAuthUseCase oauthUseCase;
    private final String frontendCallbackUrl;

    public OAuthController(
            OAuthUseCase oauthUseCase,
            @Value("${app.oauth.frontend-callback-url}") String frontendCallbackUrl) {
        this.oauthUseCase = oauthUseCase;
        this.frontendCallbackUrl = frontendCallbackUrl;
    }

    @GetMapping("/google")
    public void initiateGoogleLogin(HttpServletResponse response) throws IOException {
        String authUrl = oauthUseCase.initiateGoogleLogin(frontendCallbackUrl);
        response.sendRedirect(authUrl);
    }

    @PostMapping("/google/exchange")
    public AuthResponse exchangeGoogleCode(
            @Valid @RequestBody OAuthExchangeRequest request) {

        return oauthUseCase.handleGoogleCallback(request.code(), request.state(), frontendCallbackUrl);
    }
}