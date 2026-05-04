package com.simulacert.translation.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record TranslateFieldCommand(
        @NotBlank String entityType,
        @NotNull UUID entityId,
        @NotBlank String content,
        @NotBlank @Pattern(regexp = "[a-z]{2}(-[A-Z]{2})?", message = "language must be like en or en-US") String language
) {
}

