package br.com.simulaaws.attempt.application.dto;

import br.com.simulaaws.attempt.domain.AttemptStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AttemptResponse(
        UUID id,
        UUID userId,
        UUID examId,
        AttemptStatus status,
        Instant startedAt,
        Instant finishedAt,
        Integer score,
        List<UUID> questionIds,
        long seed
) {
}

