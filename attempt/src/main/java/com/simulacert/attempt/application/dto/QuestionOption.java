package com.simulacert.attempt.application.dto;

public record QuestionOption(
        String key,
        String text,
        boolean isCorrect
) {
}