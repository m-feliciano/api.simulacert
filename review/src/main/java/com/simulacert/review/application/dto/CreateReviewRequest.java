package com.simulacert.review.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReviewRequest(
        @Schema(description = "Attempt ID", example = "1f156fcf-cf2b-61b9-9e4f-458dfe0be681", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "attemptId is required")
        UUID attemptId,

        @Schema(description = "Rating", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "rating is required")
        @Min(value = 1, message = "rating must be at least 1")
        @Max(value = 5, message = "rating must be at most 5")
        Integer rating,

        @Schema(description = "Comment", example = "Great job!", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(max = 1000, message = "comment must not exceed 1000 characters")
        String comment
) {
}
