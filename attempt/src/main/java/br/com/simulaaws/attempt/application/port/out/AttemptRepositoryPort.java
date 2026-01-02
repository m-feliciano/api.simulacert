package br.com.simulaaws.attempt.application.port.out;

import br.com.simulaaws.attempt.domain.Attempt;
import br.com.simulaaws.attempt.domain.AttemptStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptRepositoryPort {
    Attempt save(Attempt attempt);

    Optional<Attempt> findById(UUID id);

    Optional<Attempt> findByUserIdAndExamIdAndStatus(UUID userId, UUID examId, AttemptStatus status);

    List<Attempt> findByUserIdOrderByStartedAtDesc(UUID userId);
}

