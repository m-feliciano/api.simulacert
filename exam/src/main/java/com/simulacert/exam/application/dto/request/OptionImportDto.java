package com.simulacert.exam.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OptionImportDto(
        @Schema(description = "Key", example = "A", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Option key is required")
        @Size(max = 1, message = "Key must be a single character")
        String key,

        @Schema(description = "Text", example = "Amazon Web Services", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Option text is required")
        @Size(max = 500, message = "Text must not exceed 500 characters")
        String text,

        @Schema(description = "Correct", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "isCorrect flag is required")
        Boolean correct
) {
}

