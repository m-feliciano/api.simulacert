package com.simulacert.exam.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RequestExplanationCommand(
        @Schema(description = "Question ID", example = "question-uuid-here", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID questionId,
        @Schema(description = "Exam Attempt ID", example = "attempt-uuid-here", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID examAttemptId,
        @Schema(description = "Certification", example = "AWS-Cloud-Practitioner", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Size(min = 1, max = 100) String certification
) {
}