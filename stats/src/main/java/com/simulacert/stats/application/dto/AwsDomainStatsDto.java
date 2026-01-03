package com.simulacert.stats.application.dto;

public record AwsDomainStatsDto(
        String awsDomain,
        int totalQuestions,
        int correctAnswers,
        Double accuracyRate
) {
}
