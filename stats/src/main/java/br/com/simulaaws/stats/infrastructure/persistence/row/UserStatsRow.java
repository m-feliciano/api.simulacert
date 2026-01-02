package br.com.simulaaws.stats.infrastructure.persistence.row;

import java.time.Instant;
import java.util.UUID;

public record UserStatsRow(
        UUID userId,
        int totalAttempts,
        int completedAttempts,
        Double averageScore,
        Integer bestScore,
        Instant lastAttemptAt
) {
}
