package com.simulacert.adapter.rest.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record ApiErrorResponse(
        @Schema(description = "Code", example = "validation_error") String code,
        @Schema(description = "Message", example = "Field email is required") String message,
        @Schema(description = "Timestamp", example = "2026-05-23T20:13:58Z") Instant timestamp,
        @Schema(description = "Path", example = "/api/v1/auth/register") String path) {
}

