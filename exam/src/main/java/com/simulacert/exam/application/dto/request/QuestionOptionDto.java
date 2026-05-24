package com.simulacert.exam.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionOptionDto(
        @Schema(description = "Key", example = "A", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Option key is required")
        String key,

        @Schema(description = "Text", example = "Amazon Web Services", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Option text is required")
        String text,

        @Schema(description = "Is Correct", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "isCorrect flag is required")
        Boolean isCorrect
) {
}