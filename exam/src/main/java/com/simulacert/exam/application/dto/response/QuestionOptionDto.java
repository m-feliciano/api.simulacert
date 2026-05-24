package com.simulacert.exam.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record QuestionOptionDto(
		@Schema(description = "Key", example = "A") String key,
		@Schema(description = "Text", example = "Amazon Web Services") String text,
		@Schema(description = "Is Correct", example = "true") Boolean isCorrect,
		@Schema(description = "Option ID", example = "option-uuid-here") UUID id) {
}