package com.simulacert.translation.application.dto;

import java.util.UUID;

public record TranslationResponse(
        UUID id,
        String entityType,
        UUID entityId,
        String field,
        String language,
        String value,
        String source,
        boolean reviewed
) {
}

