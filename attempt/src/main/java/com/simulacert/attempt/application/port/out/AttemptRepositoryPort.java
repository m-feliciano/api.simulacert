package com.simulacert.attempt.application.port.out;

import com.simulacert.attempt.domain.Attempt;
import com.simulacert.attempt.domain.AttemptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptRepositoryPort {
    Attempt save(Attempt attempt);

    Optional<Attempt> findById(UUID id);

    Optional<Attempt> findByUserIdAndExamIdAndStatus(UUID userId, UUID examId, AttemptStatus status);

    List<Attempt> findByStatusAndStartedAtBefore(AttemptStatus attemptStatus, Instant cutoff);

    int countByStatus(UUID userId, AttemptStatus attemptStatus);

    Page<Attempt> findByUserIdPaginated(UUID userId, Pageable pageable);
}

