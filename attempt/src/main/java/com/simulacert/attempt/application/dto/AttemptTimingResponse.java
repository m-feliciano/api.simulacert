package com.simulacert.attempt.application.dto;

public record AttemptTimingResponse(
        String endsAt,
        long remainingSeconds,
        boolean paused,
        String pausedAt
) {
}

