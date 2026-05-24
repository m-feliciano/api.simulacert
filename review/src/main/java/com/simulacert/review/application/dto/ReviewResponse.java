package com.simulacert.review.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        @Schema(description = "Review ID", example = "review-uuid-here") UUID id,
        @Schema(description = "User ID", example = "1f156fcf-cf2b-61b9-9e4f-458dfe0be681") UUID userId,
        @Schema(description = "Attempt ID", example = "attempt-uuid-here") UUID attemptId,
        @Schema(description = "Rating", example = "5") Integer rating,
        @Schema(description = "Comment", example = "Excellent attempt") String comment,
        @Schema(description = "Created At", example = "2026-05-23T20:13:58Z") Instant createdAt
) {
}

