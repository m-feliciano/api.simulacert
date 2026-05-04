CREATE TABLE translations
(
    id          UUID PRIMARY KEY,
    entity_type VARCHAR(100)  NOT NULL,
    entity_id   UUID          NOT NULL,
    content     VARCHAR(1000) NOT NULL,
    language    VARCHAR(10)   NOT NULL,
    value       TEXT          NOT NULL,
    source      VARCHAR(20)   NOT NULL,
    reviewed    BOOLEAN       NOT NULL DEFAULT FALSE,
    version     BIGINT        NOT NULL DEFAULT 0,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,

    CONSTRAINT uk_translations_entity_field_lang UNIQUE (entity_type, entity_id, content, language)
);

CREATE INDEX idx_translations_lookup ON translations (entity_type, entity_id, content, language);
CREATE INDEX idx_translations_reviewed ON translations (reviewed);

