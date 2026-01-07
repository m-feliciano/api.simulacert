package com.simulacert.attempt.application.port.out;

import com.simulacert.attempt.domain.Attempt;
import com.simulacert.attempt.domain.AttemptStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptRepositoryPort {
    Attempt save(Attempt attempt);

    Optional<Attempt> findById(UUID id);

    Optional<Attempt> findByUserIdAndExamIdAndStatus(UUID userId, UUID examId, AttemptStatus status);

    List<Attempt> findByUserIdOrderByStartedAtDesc(UUID userId);

    List<Attempt> findByStatusAndStartedAtBefore(AttemptStatus attemptStatus, Instant cutoff);
}

