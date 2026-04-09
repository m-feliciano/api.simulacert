package com.simulacert.exam.application.dto.response;

import java.util.UUID;

public record ExamResponse(
        UUID id,
        String title,
        String description,
        Double durationMinutes,
        Long totalQuestions,
        String difficulty,
        String slug
) {

    public ExamResponse(UUID uid, String title, String description) {
        this(uid, title, description, null, null, null, null);
    }
}

