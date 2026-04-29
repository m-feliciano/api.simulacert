ALTER TABLE attempts
    ADD COLUMN IF NOT EXISTS ends_at                  TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS paused                   BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS paused_at                TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS paused_remaining_seconds BIGINT    NULL;