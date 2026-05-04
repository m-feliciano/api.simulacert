package com.simulacert.exam.application.port.out;

import com.simulacert.exam.application.dto.response.QuestionOptionDto;

import java.util.List;
import java.util.UUID;

public interface QuestionOptionQueryPort {

    List<QuestionOptionDto> findByQuestionId(UUID questionId);
}