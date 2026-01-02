package br.com.simulaaws.exam.application.mapper;

import br.com.simulaaws.exam.application.dto.response.ExamResponse;
import br.com.simulaaws.exam.domain.Exam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExamMapper {

    ExamResponse toResponse(Exam exam);
}
