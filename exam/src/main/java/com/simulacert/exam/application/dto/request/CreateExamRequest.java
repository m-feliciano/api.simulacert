package com.simulacert.exam.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateExamRequest(
        @Schema(description = "Title", example = "AWS Cloud Practitioner", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,

        @Schema(description = "Description", example = "Practice exam for AWS certification", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "Slug", example = "aws-cloud-practitioner", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(max = 100, message = "Slug must not exceed 100 characters")
        String slug
) {
}
