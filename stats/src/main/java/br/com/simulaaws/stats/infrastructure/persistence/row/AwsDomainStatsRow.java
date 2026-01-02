package br.com.simulaaws.stats.infrastructure.persistence.row;

public record AwsDomainStatsRow(
        String awsDomain,
        int totalQuestions,
        int correctAnswers,
        Double accuracyRate
) {
}
