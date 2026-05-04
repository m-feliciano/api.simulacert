package com.simulacert.exam.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record RequestExplanations(
        @NotNull @Size(min = 1) List<UUID> questionIds
) {
}