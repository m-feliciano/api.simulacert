package com.simulacert.auth.application.service;

import com.simulacert.auth.application.dto.AuthResponse;
import com.simulacert.auth.application.dto.GoogleTokenResponse;
import com.simulacert.auth.application.dto.GoogleUserInfo;
import com.simulacert.auth.application.dto.UserResponse;
import com.simulacert.auth.application.mapper.UserMapper;
import com.simulacert.auth.application.port.in.OAuthUseCase;
import com.simulacert.auth.application.port.out.GoogleOAuthClientPort;
import com.simulacert.auth.application.port.out.OAuthStateRepositoryPort;
import com.simulacert.auth.application.port.out.TokenProviderPort;
import com.simulacert.auth.application.port.out.UserRepositoryPort;
import com.simulacert.auth.domain.AuthProvider;
import com.simulacert.auth.domain.OAuthState;
import com.simulacert.auth.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class OAuthService implements OAuthUseCase {

    private final UserRepositoryPort userRepository;
    private final OAuthStateRepositoryPort stateRepository;
    private final GoogleOAuthClientPort googleClient;
    private final TokenProviderPort tokenProvider;
    private final UserMapper userMapper;
    private final String googleClientId;
    private final String googleAuthUri;
    private final int stateExpirationMinutes;

    public OAuthService(
            UserRepositoryPort userRepository,
            OAuthStateRepositoryPort stateRepository,
            GoogleOAuthClientPort googleClient,
            TokenProviderPort tokenProvider,
            UserMapper userMapper,
            @Value("${app.oauth.google.client-id}") String googleClientId,
            @Value("${app.oauth.google.auth-uri:https://accounts.google.com/o/oauth2/v2/auth}") String googleAuthUri,
            @Value("${app.oauth.state-expiration-minutes:5}") int stateExpirationMinutes) {
        this.userRepository = userRepository;
        this.stateRepository = stateRepository;
        this.googleClient = googleClient;
        this.tokenProvider = tokenProvider;
        this.userMapper = userMapper;
        this.googleClientId = googleClientId;
        this.googleAuthUri = googleAuthUri;
        this.stateExpirationMinutes = stateExpirationMinutes;
    }

    @Override
    @Transactional
    public String initiateGoogleLogin(String redirectUri) {
        stateRepository.deleteExpired(Instant.now());

        String state = generateSecureState();
        OAuthState oauthState = OAuthState.create(state, AuthProvider.GOOGLE, Instant.now(), stateExpirationMinutes);
        stateRepository.save(oauthState);

        return UriComponentsBuilder.fromUriString(googleAuthUri)
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .queryParam("access_type", "online")
                .build()
                .toUriString();
    }

    @Override
    @Transactional
    public AuthResponse handleGoogleCallback(String code, String state, String redirectUri) {
        Optional<OAuthState> oauthState = stateRepository.findByStateAndProvider(state, AuthProvider.GOOGLE);

        if (oauthState.isEmpty()) throw new IllegalArgumentException("Invalid state parameter");

        if (oauthState.get().isExpired(Instant.now())) throw new IllegalArgumentException("State parameter expired");

        GoogleTokenResponse tokens = googleClient.exchangeCodeForTokens(code, redirectUri);

        GoogleUserInfo userInfo = googleClient.validateIdTokenAndExtractUserInfo(tokens.id_token());

        if (!userInfo.emailVerified()) throw new IllegalArgumentException("Email not verified by Google");

        User user = findOrCreateUser(userInfo);

        String token = tokenProvider.generateToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);
        UserResponse userResponse = userMapper.toResponse(user);

        return AuthResponse.of(token, userResponse, refreshToken);
    }

    private User findOrCreateUser(GoogleUserInfo userInfo) {
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(
                AuthProvider.GOOGLE,
                userInfo.sub()
        );

        if (existingUser.isPresent()) return existingUser.get();

        Optional<User> userByEmail = userRepository.findByEmail(userInfo.email());
        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            if (user.getProvider() == AuthProvider.LOCAL) {
                throw new IllegalArgumentException("Email already registered with local account");
            }
            return user;
        }

        User newUser = User.createFromOAuth(
                userInfo.email(),
                userInfo.name(),
                AuthProvider.GOOGLE,
                userInfo.sub(),
                Instant.now()
        );

        return userRepository.save(newUser);
    }

    private String generateSecureState() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

