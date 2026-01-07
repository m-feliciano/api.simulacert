package com.simulacert.auth.application.port.in;

import com.simulacert.auth.application.dto.AuthResponse;

public interface OAuthUseCase {

    String initiateGoogleLogin(String redirectUri);

    AuthResponse handleGoogleCallback(String code, String state, String redirectUri);
}

