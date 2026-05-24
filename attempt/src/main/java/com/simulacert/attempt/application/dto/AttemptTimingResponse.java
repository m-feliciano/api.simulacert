package com.simulacert.attempt.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AttemptTimingResponse(
        @Schema(description = "Ends At", example = "2026-05-23T21:13:58Z") String endsAt,
        @Schema(description = "Remaining Seconds", example = "3600") long remainingSeconds,
        @Schema(description = "Paused", example = "false") boolean paused,
        @Schema(description = "Paused At", example = "2026-05-23T20:13:58Z") String pausedAt) {
}

