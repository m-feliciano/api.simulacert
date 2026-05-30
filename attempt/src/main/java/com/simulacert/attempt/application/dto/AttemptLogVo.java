package com.simulacert.attempt.application.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record AttemptLogVo(
        UUID id,
        UUID examId,
        String status,
        Instant startedAt,
        Instant finishedAt,
        Instant endsAt,
        boolean paused,
        Instant pausedAt,
        Long pausedRemainingSeconds
) {

    public static AttemptLogVo fromAttemptVo(AttemptVo attemptVo) {
        return new AttemptLogVo(
                attemptVo.id(),
                attemptVo.examId(),
                attemptVo.status(),
                attemptVo.startedAt(),
                attemptVo.finishedAt(),
                attemptVo.endsAt(),
                attemptVo.paused(),
                attemptVo.pausedAt(),
                attemptVo.pausedRemainingSeconds()
        );
    }
}
