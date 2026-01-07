package com.simulacert.auth.application.port.out;

import com.simulacert.auth.application.dto.GoogleTokenResponse;
import com.simulacert.auth.application.dto.GoogleUserInfo;

public interface GoogleOAuthClientPort {

    GoogleTokenResponse exchangeCodeForTokens(String code, String redirectUri);

    GoogleUserInfo validateIdTokenAndExtractUserInfo(String idToken);
}

