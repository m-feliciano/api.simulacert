package com.simulacert.exam.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ExamResponse(
        @Schema(description = "Exam ID", example = "exam-uuid-here") UUID id,
        @Schema(description = "Title", example = "AWS Cloud Practitioner") String title,
        @Schema(description = "Description", example = "Practice exam for AWS certification") String description,
        @Schema(description = "Duration Minutes", example = "60") Double durationMinutes,
        @Schema(description = "Total Questions", example = "50") Long totalQuestions,
        @Schema(description = "Difficulty", example = "MEDIUM") String difficulty,
        @Schema(description = "Slug", example = "aws-cloud-practitioner") String slug
) {

    public ExamResponse(UUID uid, String title, String description) {
        this(uid, title, description, null, null, null, null);
    }
}

