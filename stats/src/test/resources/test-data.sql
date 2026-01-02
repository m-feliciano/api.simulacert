-- Test data for Stats queries validation
-- Users
INSERT INTO users (id, email, name, password_hash, role, active, created_at, version)
VALUES ('11111111-1111-1111-1111-111111111111', 'test@example.com', 'Test User', '$2a$10$test', 'USER', true,
        CURRENT_TIMESTAMP, 0);

-- Exams
INSERT INTO exams (id, title, description)
VALUES ('22222222-2222-2222-2222-222222222222', 'AWS Certified Solutions Architect', 'Practice exam for AWS SAA'),
       ('33333333-3333-3333-3333-333333333333', 'AWS Certified Developer', 'Practice exam for AWS DEV');

-- Questions
INSERT INTO questions (id, exam_id, text, domain, difficulty)
VALUES ('44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222', 'What is S3?', 'Storage',
        'EASY'),
       ('55555555-5555-5555-5555-555555555555', '22222222-2222-2222-2222-222222222222', 'What is EC2?', 'Compute',
        'MEDIUM');

-- Attempts
INSERT INTO attempts (id, user_id, exam_id, status, started_at, finished_at, score, seed)
VALUES ('66666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111',
        '22222222-2222-2222-2222-222222222222', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '2' DAY,
        CURRENT_TIMESTAMP - INTERVAL '2' DAY + INTERVAL '1' HOUR, 85, 123456),
       ('77777777-7777-7777-7777-777777777777', '11111111-1111-1111-1111-111111111111',
        '33333333-3333-3333-3333-333333333333', 'IN_PROGRESS', CURRENT_TIMESTAMP - INTERVAL '1' DAY, NULL, NULL,
        789012);

-- Attempt Questions
INSERT INTO attempt_questions (attempt_id, question_id)
VALUES ('66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444'),
       ('66666666-6666-6666-6666-666666666666', '55555555-5555-5555-5555-555555555555'),
       ('77777777-7777-7777-7777-777777777777', '44444444-4444-4444-4444-444444444444');

