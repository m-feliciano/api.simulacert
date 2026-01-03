package com.simulacert.exam.application.dto.response;

import java.util.UUID;

public record ExamResponse(
        UUID id,
        String title,
        String description
) {
}

