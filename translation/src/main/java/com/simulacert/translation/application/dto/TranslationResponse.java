package com.simulacert.translation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TranslationResponse(
        @Schema(description = "Translation ID", example = "uuid") UUID id,
        @Schema(description = "Entity Type", example = "QUESTION") String entityType,
        @Schema(description = "Entity ID", example = "uuid") UUID entityId,
        @Schema(description = "Field", example = "text") String field,
        @Schema(description = "Language", example = "en") String language,
        @Schema(description = "Value", example = "What does AWS stand for?") String value,
        @Schema(description = "Source", example = "manual") String source,
        @Schema(description = "Reviewed", example = "true") boolean reviewed
) {
}

