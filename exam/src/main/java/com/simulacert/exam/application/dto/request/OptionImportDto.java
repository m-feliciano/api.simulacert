package com.simulacert.exam.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OptionImportDto(
        @NotBlank(message = "Option key is required")
        @Size(max = 1, message = "Key must be a single character")
        String key,

        @NotBlank(message = "Option text is required")
        @Size(max = 500, message = "Text must not exceed 500 characters")
        String text,

        @NotNull(message = "isCorrect flag is required")
        Boolean isCorrect
) {
}

