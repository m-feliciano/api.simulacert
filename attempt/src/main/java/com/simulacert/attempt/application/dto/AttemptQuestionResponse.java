package com.simulacert.attempt.application.dto;

import java.util.List;
import java.util.UUID;

public record AttemptQuestionResponse(
        UUID questionId,
        String questionCode,
        String text,
        String domain,
        String difficulty,
        List<QuestionOption> options,
        String selectedOption
) {
}

