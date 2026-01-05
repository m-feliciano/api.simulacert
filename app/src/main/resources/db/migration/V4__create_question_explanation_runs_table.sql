CREATE TABLE if not exists question_explanation_runs
(
    id              UUID PRIMARY KEY,
    question_id     UUID             NOT NULL,
    exam_attempt_id UUID,
    model_provider  VARCHAR(50)      NOT NULL,
    model_name      VARCHAR(100)     NOT NULL,
    prompt_version  VARCHAR(20)      NOT NULL,
    temperature     DOUBLE PRECISION NOT NULL,
    language        VARCHAR(10)      NOT NULL,
    content         TEXT             NOT NULL,
    user_rating     INTEGER CHECK (user_rating >= 1 AND user_rating <= 5),
    user_feedback   VARCHAR(1000),
    rated_at        TIMESTAMP,
    created_at      TIMESTAMP        NOT NULL,
    expires_at      TIMESTAMP        NOT NULL
);

ALTER TABLE question_explanation_runs
    ADD CONSTRAINT fk_explanation_runs_questions FOREIGN KEY (question_id) REFERENCES questions (id);
ALTER TABLE question_explanation_runs
    ADD CONSTRAINT fk_explanation_runs_attempts FOREIGN KEY (exam_attempt_id) REFERENCES attempts (id);

CREATE INDEX idx_explanation_runs_question_id ON question_explanation_runs (question_id);
CREATE INDEX idx_explanation_runs_attempt_id ON question_explanation_runs (exam_attempt_id);
CREATE INDEX idx_explanation_runs_expires_at ON question_explanation_runs (expires_at);

