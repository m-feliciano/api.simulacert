package com.simulacert.exam.application.mapper;

import com.simulacert.exam.application.dto.response.QuestionResponse;
import com.simulacert.exam.domain.Question;
import com.simulacert.exam.domain.QuestionExplanationRun;
import com.simulacert.llm.application.dto.ExplanationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QuestionMapper {

    QuestionResponse toResponse(Question question);

    List<QuestionResponse> toResponseList(List<Question> questions);

    @Mapping(target = "model", source = "modelName")
    @Mapping(target = "explanationId", source = "id")
    ExplanationResponse toExplanationResponse(QuestionExplanationRun question);
}
