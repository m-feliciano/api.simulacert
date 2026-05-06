package com.simulacert.exam.application.dto.response;

import java.util.UUID;

public record QuestionOptionDto(String key, String text, Boolean isCorrect, UUID id) {
}