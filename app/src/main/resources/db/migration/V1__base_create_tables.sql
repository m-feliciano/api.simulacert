CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);

-- Exams table
CREATE TABLE exams (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000)
);

CREATE INDEX idx_exams_title ON exams(title);

-- Questions table
CREATE TABLE questions (
    id UUID PRIMARY KEY,
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    text VARCHAR(2000) NOT NULL,
    domain VARCHAR(100) NOT NULL,
    difficulty VARCHAR(50) NOT NULL
);

CREATE INDEX idx_questions_exam_id ON questions(exam_id);
CREATE INDEX idx_questions_domain ON questions(domain);
CREATE INDEX idx_questions_difficulty ON questions(difficulty);

-- Question Options table (supports multiple correct answers)
CREATE TABLE question_options (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_key CHAR(1) NOT NULL,
    option_text VARCHAR(500) NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT unique_question_option_key UNIQUE (question_id, option_key)
);

CREATE INDEX idx_question_options_question_id ON question_options(question_id);
CREATE INDEX idx_question_options_correct ON question_options(question_id, is_correct);


-- Attempts table
CREATE TABLE attempts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    exam_id UUID NOT NULL REFERENCES exams(id),
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    score INTEGER,
    seed BIGINT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_attempts_user_id ON attempts(user_id);
CREATE INDEX idx_attempts_exam_id ON attempts(exam_id);
CREATE INDEX idx_attempts_status ON attempts(status);
CREATE INDEX idx_attempts_user_exam_status ON attempts(user_id, exam_id, status);
CREATE INDEX idx_attempts_started_at ON attempts(started_at DESC);

-- Attempt Questions junction table (stores selected questions for each attempt)
CREATE TABLE attempt_questions (
    attempt_id UUID NOT NULL REFERENCES attempts(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id),
    PRIMARY KEY (attempt_id, question_id)
);

CREATE INDEX idx_attempt_questions_attempt_id ON attempt_questions(attempt_id);
CREATE INDEX idx_attempt_questions_question_id ON attempt_questions(question_id);

-- Answers table (stores user responses)
CREATE TABLE answers (
    id UUID PRIMARY KEY,
    attempt_id UUID NOT NULL REFERENCES attempts(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id),
    selected_option VARCHAR(10) NOT NULL,
    answered_at TIMESTAMP NOT NULL,
    CONSTRAINT unique_attempt_question_answer UNIQUE (attempt_id, question_id)
);

CREATE INDEX idx_answers_attempt_id ON answers(attempt_id);
CREATE INDEX idx_answers_question_id ON answers(question_id);
CREATE INDEX idx_answers_answered_at ON answers(answered_at);