package br.com.simulaaws.attempt.infrastructure.persistence.repository;

import br.com.simulaaws.attempt.domain.Attempt;
import br.com.simulaaws.attempt.domain.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {

    Optional<Attempt> findByUserIdAndExamIdAndStatus(UUID userId, UUID examId, AttemptStatus status);

    List<Attempt> findByUserIdOrderByStartedAtDesc(UUID userId);
}

