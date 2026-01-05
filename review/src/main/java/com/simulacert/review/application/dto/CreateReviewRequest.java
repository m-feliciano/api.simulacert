package com.simulacert.review.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReviewRequest(
    @NotNull(message = "attemptId is required")
    UUID attemptId,

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be at least 1")
    @Max(value = 5, message = "rating must be at most 5")
    Integer rating,

    @Size(max = 1000, message = "comment must not exceed 1000 characters")
    String comment
) {
}
