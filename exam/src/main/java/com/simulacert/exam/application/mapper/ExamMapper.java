package com.simulacert.exam.application.mapper;

import com.simulacert.exam.application.dto.response.ExamResponse;
import com.simulacert.exam.domain.Exam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExamMapper {

    ExamResponse toResponse(Exam exam);

    ExamResponse toResponseComplete(Exam exam, Long totalQuestions, Double durationMinutes, String difficultyLevel);
}
