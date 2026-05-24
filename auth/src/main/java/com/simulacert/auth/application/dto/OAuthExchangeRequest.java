
package com.simulacert.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record OAuthExchangeRequest(
        @Schema(description = "Authorization Code", example = "AQABAAIAAAAmgA...")
        @NotNull String code,
        @Schema(description = "State", example = "123456")
        @NotNull String state
) {}
