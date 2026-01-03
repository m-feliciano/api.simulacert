package com.simulacert.stats.infrastructure.persistence.row;

import java.time.Instant;
import java.util.UUID;

public record AttemptHistoryRow(
        UUID attemptId,
        UUID examId,
        String examTitle,
        Instant startedAt,
        Instant finishedAt,
        Integer score,
        String status
) {
}
