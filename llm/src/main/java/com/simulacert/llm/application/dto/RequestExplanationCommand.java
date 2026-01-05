package com.simulacert.llm.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RequestExplanationCommand(
        @NotNull UUID questionId,
        @NotNull UUID examAttemptId,
        @NotNull @Size(min = 2, max = 10) String language,
        @NotNull @Size(min = 1, max = 100) String certification
) {
}