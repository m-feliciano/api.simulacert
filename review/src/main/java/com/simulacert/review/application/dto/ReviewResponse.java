package com.simulacert.review.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID userId,
    UUID attemptId,
    Integer rating,
    String comment,
    Instant createdAt
) {
}

