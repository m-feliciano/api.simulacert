CREATE TABLE user_feedback
(
    id                          UUID PRIMARY KEY,
    user_id                     UUID,
    user_rating                 INTEGER,
    feedback                    VARCHAR(1000),
    rated_at                    TIMESTAMP,
    question_explanation_run_id UUID,
    CONSTRAINT fk_question_explanation_run
        FOREIGN KEY (question_explanation_run_id)
            REFERENCES question_explanation_runs (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_user_feedback_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);