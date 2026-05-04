ALTER table questions
    ADD COLUMN IF NOT EXISTS language VARCHAR(10) NOT NULL DEFAULT 'pt_br';

UPDATE questions
SET language = 'pt_br'
WHERE language IS NULL;