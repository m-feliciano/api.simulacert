
package com.simulacert.adapter.rest.dto;

import jakarta.validation.constraints.NotNull;

public record OAuthExchangeRequest(
        @NotNull String code,
        @NotNull String state
) {}
