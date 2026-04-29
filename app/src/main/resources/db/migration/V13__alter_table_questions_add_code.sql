ALTER TABLE questions ADD COLUMN code VARCHAR(20);
CREATE INDEX idx_questions_code ON questions(code);
