package com.simulacert.attempt.infrastructure.persistence.repository;

import com.simulacert.attempt.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    boolean existsByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);

    List<Answer> findByAttemptId(UUID attemptId);

    @Query(value = "SELECT count_correct_answers(:attemptId)", nativeQuery = true)
    long countCorrectAnswersByAttemptId(@Param("attemptId") UUID attemptId);

    @Modifying
    @Query("DELETE FROM Answer a WHERE a.attemptId = :attemptId AND a.questionId = :questionId")
    void deleteByAttemptIdAndQuestionId(@Param("attemptId") UUID attemptId,
                                        @Param("questionId") UUID questionId);
}
