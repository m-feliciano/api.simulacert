package com.simulacert.attempt.application.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record AttemptVo(
        UUID id,
        UUID examId,
        UUID userId,
        List<UUID> questionIds,
        String status,
        Integer score,
        Instant startedAt,
        Instant finishedAt,
        Instant endsAt,
        boolean paused,
        Instant pausedAt,
        Long pausedRemainingSeconds,
        String mode
) {

    public AttemptVo(UUID id,
                     UUID examId,
                     UUID userId,
                     List<UUID> questionIds,
                     String status,
                     Integer score,
                     Instant startedAt,
                     Instant finishedAt,
                     Instant endsAt,
                     boolean paused,
                     Instant pausedAt,
                     Long pausedRemainingSeconds) {
        this(id, examId, userId, questionIds, status, score, startedAt, finishedAt, endsAt, paused, pausedAt, pausedRemainingSeconds, null);
    }
}
