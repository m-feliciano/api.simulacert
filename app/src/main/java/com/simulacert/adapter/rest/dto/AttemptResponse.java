package com.simulacert.adapter.rest.dto;

import com.simulacert.attempt.domain.AttemptStatus;

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

