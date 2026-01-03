package com.simulacert.stats.application.dto;

import java.time.Instant;
import java.util.UUID;

public record UserStatsDto(
        UUID userId,
        int totalAttempts,
        int completedAttempts,
        Double averageScore,
        Integer bestScore,
        Instant lastAttemptAt
) {
}
