package com.simulacert.attempt.application.dto;

import com.simulacert.exam.application.dto.response.QuestionOptionDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record AttemptQuestionResponse(
        @Schema(description = "Question ID", example = "question-uuid-here") UUID questionId,
        @Schema(description = "Question Code", example = "Q-001") String questionCode,
        @Schema(description = "Text", example = "What does AWS stand for?") String text,
        @Schema(description = "Domain", example = "cloud") String domain,
        @Schema(description = "Difficulty", example = "EASY") String difficulty,
        List<QuestionOptionDto> options,
        @Schema(description = "Selected Option", example = "A") String selectedOption
) {
}

