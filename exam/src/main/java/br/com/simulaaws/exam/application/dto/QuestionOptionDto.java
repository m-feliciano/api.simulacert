package br.com.simulaaws.exam.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionOptionDto(
        @NotBlank(message = "Option key is required")
        String key,

        @NotBlank(message = "Option text is required")
        String text,

        @NotNull(message = "isCorrect flag is required")
        Boolean isCorrect
) {
}