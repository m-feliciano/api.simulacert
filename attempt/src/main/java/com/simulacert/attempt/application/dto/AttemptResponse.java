package com.simulacert.attempt.application.dto;

import com.simulacert.attempt.domain.AttemptStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AttemptResponse(
        @Schema(description = "Attempt ID", example = "attempt-uuid-here") UUID id,
        @Schema(description = "User ID", example = "1f156fcf-cf2b-61b9-9e4f-458dfe0be681") UUID userId,
        @Schema(description = "Exam ID", example = "exam-uuid-here") UUID examId,
        @Schema(description = "Status", example = "IN_PROGRESS") AttemptStatus status,
        @Schema(description = "Started At", example = "2026-05-23T20:13:58Z") Instant startedAt,
        @Schema(description = "Finished At", example = "2026-05-23T20:13:58Z") Instant finishedAt,
        @Schema(description = "Score", example = "80") Integer score,
        @Schema(description = "Question IDs", example = "[\"question-uuid-here\"]") List<UUID> questionIds,
        @Schema(description = "Seed", example = "123456789") long seed,
        @Schema(description = "Ends At", example = "2026-05-23T21:13:58Z") Instant endsAt,
        @Schema(description = "Mode", example = "PRACTICE") String mode) {

    public AttemptResponse(UUID id,
                           UUID userId,
                           UUID examId,
                           AttemptStatus status,
                           Instant startedAt,
                           Instant finishedAt,
                           Integer score,
                           List<UUID> questionIds,
                           long seed,
                           Instant endsAt) {
        this(id, userId, examId, status, startedAt, finishedAt, score, questionIds, seed, endsAt, null);
    }
}

