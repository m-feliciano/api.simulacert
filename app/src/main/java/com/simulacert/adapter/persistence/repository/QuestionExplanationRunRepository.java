package com.simulacert.adapter.persistence.repository;

import com.simulacert.llm.domain.QuestionExplanationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface QuestionExplanationRunRepository extends JpaRepository<QuestionExplanationRun, UUID> {

    @Modifying
    @Query("DELETE FROM QuestionExplanationRun q WHERE q.expiresAt < :now")
    void deleteByExpiresAtBefore(@Param("now") Instant now);
}