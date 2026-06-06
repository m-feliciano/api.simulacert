CREATE OR REPLACE FUNCTION count_correct_answers(
    p_attempt_id UUID
)
RETURNS BIGINT
LANGUAGE sql
AS $$
WITH attempt_questions_list AS (
    SELECT question_id
    FROM attempt_questions
    WHERE attempt_id = p_attempt_id
),
correct_options AS (
    SELECT
        aq.question_id,
        STRING_AGG(qo.option_key, ',' ORDER BY qo.option_key) AS correct_keys
    FROM attempt_questions_list aq
    JOIN question_options qo
        ON qo.question_id = aq.question_id
       AND qo.is_correct = true
    GROUP BY aq.question_id
),
user_answers_expanded AS (
    SELECT
        a.question_id,
        UPPER(TRIM(unnested.option_key)) AS option_key
    FROM answers a
    CROSS JOIN LATERAL unnest(
        string_to_array(a.selected_option, ',')
    ) AS unnested(option_key)
    WHERE a.attempt_id = p_attempt_id
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
         JOIN correct_options co
              ON ua.question_id = co.question_id
WHERE ua.user_keys = co.correct_keys;
$$;