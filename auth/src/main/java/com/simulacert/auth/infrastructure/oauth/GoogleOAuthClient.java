package com.simulacert.auth.infrastructure.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simulacert.auth.application.dto.GoogleTokenResponse;
import com.simulacert.auth.application.dto.GoogleUserInfo;
import com.simulacert.auth.application.port.out.GoogleOAuthClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class GoogleOAuthClient implements GoogleOAuthClientPort {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String clientId;
    private final String clientSecret;
    private final String tokenUri;
    private final String jwksUri;
    private final Map<String, PublicKey> keyCache;

    public GoogleOAuthClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.oauth.google.client-id}") String clientId,
            @Value("${app.oauth.google.client-secret}") String clientSecret,
            @Value("${app.oauth.google.token-uri:https://oauth2.googleapis.com/token}") String tokenUri,
            @Value("${app.oauth.google.jwks-uri:https://www.googleapis.com/oauth2/v3/certs}") String jwksUri) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUri = tokenUri;
        this.jwksUri = jwksUri;
        this.keyCache = new HashMap<>();
    }

    @Override
    public GoogleTokenResponse exchangeCodeForTokens(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<GoogleTokenResponse> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                request,
                GoogleTokenResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to exchange code for tokens");
        }

        return response.getBody();
    }

    @Override
    public GoogleUserInfo validateIdTokenAndExtractUserInfo(String idToken) {
        String[] parts = idToken.split("\\.");
        if (parts.length != 3) {
            throw new RuntimeException("Invalid ID token format");
        }

        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        byte[] signature = Base64.getUrlDecoder().decode(parts[2]);

        try {
            JsonNode header = objectMapper.readTree(headerJson);
            JsonNode payload = objectMapper.readTree(payloadJson);

            String kid = header.get("kid").asText();
            String alg = header.get("alg").asText();

            if (!"RS256".equals(alg)) {
                throw new RuntimeException("Unsupported algorithm: " + alg);
            }

            String iss = payload.get("iss").asText();
            if (!"https://accounts.google.com".equals(iss) && !"accounts.google.com".equals(iss)) {
                throw new RuntimeException("Invalid issuer: " + iss);
            }

            String aud = payload.get("aud").asText();
            if (!clientId.equals(aud)) {
                throw new RuntimeException("Invalid audience: " + aud);
            }

            long exp = payload.get("exp").asLong();
            if (System.currentTimeMillis() / 1000 > exp) {
                throw new RuntimeException("Token expired");
            }

            PublicKey publicKey = getPublicKey(kid);
            boolean valid = verifySignature(parts[0] + "." + parts[1], signature, publicKey);

            if (!valid) throw new RuntimeException("Invalid token signature");

            String sub = payload.get("sub").asText();
            String email = payload.get("email").asText();
            boolean emailVerified = payload.get("email_verified").asBoolean();
            String name = payload.has("name") ? payload.get("name").asText() : email;
            String picture = payload.has("picture") ? payload.get("picture").asText() : null;

            return new GoogleUserInfo(sub, email, emailVerified, name, picture);

        } catch (Exception e) {
            throw new RuntimeException("Failed to validate ID token: " + e.getMessage(), e);
        }
    }

    private PublicKey getPublicKey(String kid) {
        if (keyCache.containsKey(kid)) return keyCache.get(kid);

        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(jwksUri, JsonNode.class);
            JsonNode jwks = response.getBody();

            if (jwks == null || !jwks.has("keys")) throw new RuntimeException("Failed to fetch JWKs");

            for (JsonNode key : jwks.get("keys")) {
                String keyId = key.get("kid").asText();
                if (kid.equals(keyId)) {
                    String n = key.get("n").asText();
                    String e = key.get("e").asText();

                    PublicKey publicKey = buildRSAPublicKey(n, e);
                    keyCache.put(kid, publicKey);
                    return publicKey;
                }
            }

            throw new RuntimeException("Public key not found for kid: " + kid);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch public key: " + ex.getMessage(), ex);
        }
    }

    private PublicKey buildRSAPublicKey(String modulusBase64, String exponentBase64) throws Exception {
        byte[] modulusBytes = Base64.getUrlDecoder().decode(modulusBase64);
        byte[] exponentBytes = Base64.getUrlDecoder().decode(exponentBase64);

        java.math.BigInteger modulus = new java.math.BigInteger(1, modulusBytes);
        java.math.BigInteger exponent = new java.math.BigInteger(1, exponentBytes);

        java.security.spec.RSAPublicKeySpec spec = new java.security.spec.RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    private boolean verifySignature(String data, byte[] signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes(StandardCharsets.UTF_8));
            return sig.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }
}

