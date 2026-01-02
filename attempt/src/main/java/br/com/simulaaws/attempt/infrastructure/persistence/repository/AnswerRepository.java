package br.com.simulaaws.attempt.infrastructure.persistence.repository;

import br.com.simulaaws.attempt.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
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
        WITH correct_options AS (
            SELECT question_id, STRING_AGG(option_key, ',' ORDER BY option_key) AS correct_keys
            FROM question_options
            WHERE is_correct = true
            GROUP BY question_id
        ),
        user_answers AS (
            SELECT 
                a.question_id,
                STRING_AGG(UPPER(TRIM(unnest(string_to_array(a.selected_option, ',')))), ',' ORDER BY UPPER(TRIM(unnest(string_to_array(a.selected_option, ','))))) AS user_keys
            FROM answers a
            WHERE a.attempt_id = :attemptId
            GROUP BY a.question_id
        )
        SELECT COUNT(*)
        FROM user_answers ua
        JOIN correct_options co ON ua.question_id = co.question_id
        WHERE ua.user_keys = co.correct_keys
        """, nativeQuery = true)
    long countCorrectAnswersByAttemptId(@Param("attemptId") UUID attemptId);
}
