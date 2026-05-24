package com.simulacert.review.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReviewSummaryResponse(
        @Schema(description = "Submitted", example = "10") long submitted,
        @Schema(description = "Detailed", example = "8") long detailed,
        @Schema(description = "Useful", example = "7") Long useful,
        @Schema(description = "Approved", example = "6") Long approved
) {
}