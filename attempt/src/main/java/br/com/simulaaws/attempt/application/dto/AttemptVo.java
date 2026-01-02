package br.com.simulaaws.attempt.application.dto;

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
        Instant finishedAt
) {
}
