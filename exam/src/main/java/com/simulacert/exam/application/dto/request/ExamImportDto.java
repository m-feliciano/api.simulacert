package com.simulacert.exam.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ExamImportDto(
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Size(max = 100, message = "Slug must not exceed 100 characters")
        String slug,

        @NotEmpty(message = "Questions are required")
        @Valid
        List<QuestionImportDto> questions
) {
}

