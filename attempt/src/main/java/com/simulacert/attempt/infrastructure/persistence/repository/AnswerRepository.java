package com.simulacert.attempt.infrastructure.persistence.repository;

import com.simulacert.attempt.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    boolean existsByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);

    List<Answer> findByAttemptId(UUID attemptId);

    @Query(value = """
        WITH attempt_questions_list AS (
            SELECT question_id
            FROM attempt_questions
            WHERE attempt_id = :attemptId
        ),
        correct_options AS (
            SELECT
                aq.question_id, 
                STRING_AGG(qo.option_key, ',' ORDER BY qo.option_key) AS correct_keys
            FROM attempt_questions_list aq
            JOIN question_options qo ON qo.question_id = aq.question_id AND qo.is_correct = true
            GROUP BY aq.question_id
        ),
        user_answers_expanded AS (
            SELECT
                a.question_id,
                UPPER(TRIM(unnested.option_key)) AS option_key
            FROM answers a
            CROSS JOIN LATERAL unnest(string_to_array(a.selected_option, ',')) AS unnested(option_key)
            WHERE a.attempt_id = :attemptId
        ),
        user_answers AS (
            SELECT
                question_id,
                STRING_AGG(option_key, ',' ORDER BY option_key) AS user_keys
            FROM user_answers_expanded
            GROUP BY question_id
        )
        SELECT COUNT(*)
        FROM user_answers ua
        JOIN correct_options co ON ua.question_id = co.question_id
        WHERE ua.user_keys = co.correct_keys
        """, nativeQuery = true)
    long countCorrectAnswersByAttemptId(@Param("attemptId") UUID attemptId);

    @Modifying
    @Query("DELETE FROM Answer a WHERE a.attemptId = :attemptId AND a.questionId = :questionId")
    void deleteByAttemptIdAndQuestionId(@Param("attemptId") UUID attemptId,
                                        @Param("questionId") UUID questionId);
}
