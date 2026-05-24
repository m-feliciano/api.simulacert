package com.simulacert.exam.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateExamRequest(
        @Schema(description = "Title", example = "AWS Cloud Practitioner", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,

        @Schema(description = "Description", example = "Updated exam description", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {
}

