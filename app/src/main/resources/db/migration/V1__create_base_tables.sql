CREATE TABLE exams (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000)
);
CREATE TABLE questions (
    id UUID PRIMARY KEY,
    exam_id UUID NOT NULL REFERENCES exams(id),
    text VARCHAR(2000) NOT NULL,
    domain VARCHAR(100) NOT NULL,
    difficulty VARCHAR(50) NOT NULL
);
CREATE INDEX idx_questions_exam_id ON questions(exam_id);
CREATE TABLE attempts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    exam_id UUID NOT NULL REFERENCES exams(id),
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    score INTEGER,
    seed BIGINT NOT NULL
);
CREATE INDEX idx_attempts_user_exam_status ON attempts(user_id, exam_id, status);
CREATE TABLE attempt_questions (
    attempt_id UUID NOT NULL REFERENCES attempts(id),
    question_id UUID NOT NULL
);
CREATE INDEX idx_attempt_questions_attempt_id ON attempt_questions(attempt_id);
