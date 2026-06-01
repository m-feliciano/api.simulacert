ALTER TABLE question_explanation_runs
ALTER
COLUMN prompt_version TYPE VARCHAR(100) USING prompt_version::VARCHAR(100);