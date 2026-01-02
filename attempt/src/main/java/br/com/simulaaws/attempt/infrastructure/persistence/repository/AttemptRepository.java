package br.com.simulaaws.attempt.infrastructure.persistence.repository;

import br.com.simulaaws.attempt.domain.Attempt;
import br.com.simulaaws.attempt.domain.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {

    @Query("SELECT a FROM Attempt a WHERE a.userId = :userId AND a.examId = :examId AND a.status = :status")
    Optional<Attempt> findByUserIdAndExamIdAndStatus(
            @Param("userId") UUID userId,
            @Param("examId") UUID examId,
            @Param("status") AttemptStatus status
    );

    List<Attempt> findByUserIdOrderByStartedAtDesc(UUID userId);
}

