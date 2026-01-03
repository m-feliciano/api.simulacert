package com.simulacert.exam.application.port.out;

import java.util.List;
import java.util.UUID;

public interface QuestionOptionQueryPort {

    List<QuestionOptionDto> findByQuestionId(UUID questionId);

    record QuestionOptionDto(String key, String text, Boolean isCorrect) {}
}