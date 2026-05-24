package com.simulacert.exam.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionImportDto(
        @Schema(description = "Text", example = "What does AWS stand for?", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Text is required")
        @Size(min = 10, max = 2000, message = "Text must be between 10 and 2000 characters")
        String text,

        @Schema(description = "Difficulty", example = "EASY", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Difficulty is required")
        @Size(max = 50, message = "Difficulty must not exceed 50 characters")
        String difficulty,

        @Schema(description = "Domain", example = "cloud", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Domain is required")
        @Size(max = 100, message = "Domain must not exceed 100 characters")
        String domain,

        @Schema(description = "Code", example = "Q-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Code is required")
        @Size(max = 20, message = "Code must not exceed 20 characters")
        String code,

        @Schema(description = "Options", example = "[{\"key\":\"A\",\"text\":\"Amazon Web Services\",\"correct\":true}]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "Options are required")
        @Valid
        List<OptionImportDto> options
) {
}

