package com.simulacert.exam.infrastructure.persistence.repository;

import com.simulacert.exam.domain.QuestionExplanationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionExplanationRunRepository extends JpaRepository<QuestionExplanationRun, UUID> {

    @Modifying
    @Query("DELETE FROM QuestionExplanationRun q WHERE q.expiresAt < :now")
    void deleteByExpiresAtBefore(@Param("now") Instant now);

    @Query("SELECT q FROM QuestionExplanationRun q WHERE q.questionId = :questionId AND q.language = :language and q.expiresAt > :now")
    Optional<List<QuestionExplanationRun>> findByQuestionIdAndLanguage(
            @Param("questionId") UUID questionId,
            @Param("language") String language,
            @Param("now") Instant now
    );

    @Query("SELECT q FROM QuestionExplanationRun q WHERE q.questionId IN :questionIds AND q.language = :language and q.expiresAt > :now")
    List<QuestionExplanationRun> findByQuestionIdsAndExamId(
            @Param("questionIds") List<UUID> questionIds,
            @Param("language") String language,
            @Param("now") Instant now
    );
}