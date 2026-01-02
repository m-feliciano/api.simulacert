package br.com.simulaaws.exam.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateQuestionRequest(
        @NotNull(message = "Exam ID is required")
        java.util.UUID examId,

        @NotBlank(message = "Text is required")
        @Size(min = 10, max = 2000, message = "Text must be between 10 and 2000 characters")
        String text,

        @NotBlank(message = "Domain is required")
        @Size(max = 100, message = "Domain must not exceed 100 characters")
        String domain,

        @NotBlank(message = "Difficulty is required")
        @Size(max = 50, message = "Difficulty must not exceed 50 characters")
        String difficulty
) {
}

