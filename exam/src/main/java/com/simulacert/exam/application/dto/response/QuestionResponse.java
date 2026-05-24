package com.simulacert.exam.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record QuestionResponse(
        @Schema(description = "Question ID", example = "uuid") UUID id,
        @Schema(description = "Exam ID", example = "uuid") UUID examId,
        @Schema(description = "Text", example = "What does AWS stand for?") String text,
        @Schema(description = "Domain", example = "cloud") String domain,
        @Schema(description = "Difficulty", example = "EASY") String difficulty,
        @Schema(description = "Code", example = "Q-001") String code,
        List<QuestionOptionResponse> options
) {
}

