package com.simulacert.exam.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record QuestionOptionResponse(
        @Schema(description = "Option Key", example = "A") String optionKey,
        @Schema(description = "Option Text", example = "Amazon Web Services") String optionText,
        @Schema(description = "Is Correct", example = "true") Boolean isCorrect
) {
}
