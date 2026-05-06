package com.simulacert.exam.infrastructure.persistence.repository;

import com.simulacert.exam.domain.QuestionExplanationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuestionExplanationRunRepository extends JpaRepository<QuestionExplanationRun, UUID> {

    @Query("SELECT q FROM QuestionExplanationRun q WHERE q.questionId = :questionId")
    List<QuestionExplanationRun> findAllByQuestionId(@Param("questionId") UUID questionId);
}