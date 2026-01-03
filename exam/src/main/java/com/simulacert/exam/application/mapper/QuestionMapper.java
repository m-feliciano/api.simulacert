package com.simulacert.exam.application.mapper;

import com.simulacert.exam.application.dto.response.QuestionResponse;
import com.simulacert.exam.domain.Question;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QuestionMapper {

    QuestionResponse toResponse(Question question);

    List<QuestionResponse> toResponseList(List<Question> questions);
}
