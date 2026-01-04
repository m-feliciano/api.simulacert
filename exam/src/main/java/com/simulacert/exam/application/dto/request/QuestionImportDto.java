package com.simulacert.exam.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionImportDto(
        @NotBlank(message = "Text is required")
        @Size(min = 10, max = 2000, message = "Text must be between 10 and 2000 characters")
        String text,

        @NotBlank(message = "Difficulty is required")
        @Size(max = 50, message = "Difficulty must not exceed 50 characters")
        String difficulty,

        @NotBlank(message = "Domain is required")
        @Size(max = 100, message = "Domain must not exceed 100 characters")
        String domain,

        @NotEmpty(message = "Options are required")
        @Valid
        List<OptionImportDto> options
) {
}

