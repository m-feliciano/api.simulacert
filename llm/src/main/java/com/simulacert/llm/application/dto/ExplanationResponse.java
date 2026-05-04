package com.simulacert.llm.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ExplanationResponse(
        UUID explanationId,
        UUID questionId,
        String content,
        String model,
        Instant expiresAt
) {
}