package com.simulacert.llm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record ExplanationResponse(
        @Schema(description = "Explanation ID", example = "explanation-uuid-here") UUID explanationId,
        @Schema(description = "Question ID", example = "question-uuid-here") UUID questionId,
        @Schema(description = "Content", example = "AWS stands for Amazon Web Services.") String content,
        @Schema(description = "Model", example = "gpt-4.1-mini") String model,
        @Schema(description = "Expires At", example = "2026-05-24T20:13:58Z") Instant expiresAt
) {
}