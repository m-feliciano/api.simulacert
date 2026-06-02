package com.simulacert.attempt.infrastructure.persistence.repository;

import com.simulacert.attempt.domain.Attempt;
import com.simulacert.attempt.domain.AttemptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {

    @EntityGraph(attributePaths = "questionIds")
    Optional<Attempt> findByUserIdAndExamIdAndStatus(
            UUID userId,
            UUID examId,
            AttemptStatus status
    );

    @EntityGraph(attributePaths = "questionIds")
    List<Attempt> findByStatusAndStartedAtBefore(
            AttemptStatus status,
            Instant cutoff
    );

    int countByUserIdAndStatus(
            UUID userId,
            AttemptStatus status
    );

    @EntityGraph(attributePaths = "questionIds")
    Page<Attempt> findByUserId(
            UUID userId,
            Pageable pageable
    );
}

