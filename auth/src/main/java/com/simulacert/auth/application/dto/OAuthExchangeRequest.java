
package com.simulacert.auth.application.dto;

import jakarta.validation.constraints.NotNull;

public record OAuthExchangeRequest(
        @NotNull String code,
        @NotNull String state
) {}
