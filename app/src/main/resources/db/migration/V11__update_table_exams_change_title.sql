ALTER TABLE exams
    ADD COLUMN difficulty VARCHAR(10);

UPDATE exams
SET title      = 'AWS Certified Cloud Practitioner (CLF-C02)',
    difficulty = 'EASY',
    slug       = 'aws-certified-cloud-practitioner-clf-c02'
WHERE title = 'AWS Cloud Practitioner Exam Prep (PT-BR)';

