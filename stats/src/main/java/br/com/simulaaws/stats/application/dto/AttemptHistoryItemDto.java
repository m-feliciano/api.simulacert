package br.com.simulaaws.stats.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AttemptHistoryItemDto(
        UUID attemptId,
        UUID examId,
        String examTitle,
        Instant startedAt,
        Instant finishedAt,
        Integer score,
        String status
) {
}
