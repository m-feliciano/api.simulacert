UPDATE question_explanation_runs
SET expires_at = CURRENT_TIMESTAMP
WHERE expires_at > CURRENT_TIMESTAMP;