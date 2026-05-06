package com.simulacert.attempt.application.dto;

import com.simulacert.exam.application.dto.response.QuestionOptionDto;

import java.util.List;
import java.util.UUID;

public record AttemptQuestionResponse(
        UUID questionId,
        String questionCode,
        String text,
        String domain,
        String difficulty,
        List<QuestionOptionDto> options,
        String selectedOption
) {
}

