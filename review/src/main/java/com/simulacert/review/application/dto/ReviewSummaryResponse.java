package com.simulacert.review.application.dto;

public record ReviewSummaryResponse(
        long submitted,
        long detailed,
        Long useful,
        Long approved
) {
}