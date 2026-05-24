package com.simulacert.exam.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ExamImportDto(
        @Schema(description = "Title", example = "AWS Cloud Practitioner", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,

        @Schema(description = "Description", example = "Practice exam for AWS certification", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "Slug", example = "aws-cloud-practitioner", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(max = 100, message = "Slug must not exceed 100 characters")
        String slug,

        @Schema(description = "Questions", example = "[{\"text\":\"What does AWS stand for?\",\"difficulty\":\"EASY\",\"domain\":\"cloud\",\"code\":\"Q-001\",\"options\":[{\"key\":\"A\",\"text\":\"Amazon Web Services\",\"correct\":true}]}]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "Questions are required")
        @Valid
        List<QuestionImportDto> questions
) {
}

