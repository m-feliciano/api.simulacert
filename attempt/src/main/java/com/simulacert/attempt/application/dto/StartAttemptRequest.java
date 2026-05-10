package com.simulacert.attempt.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record StartAttemptRequest(
        @NotNull UUID userId,
        @NotNull UUID examId,
        @NotNull int questionCount,
        @NotNull @Positive Integer limitSeconds,
        String difficulty,
        String mode
) {

    public StartAttemptRequest(UUID userId, UUID examId, int questionCount, Integer limitSeconds) {
        this(userId, examId, questionCount, limitSeconds, null, null);
    }
}
