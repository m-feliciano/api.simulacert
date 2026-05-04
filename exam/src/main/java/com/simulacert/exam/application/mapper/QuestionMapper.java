package com.simulacert.exam.application.mapper;

import com.simulacert.exam.application.dto.response.QuestionResponse;
import com.simulacert.exam.domain.Question;
import com.simulacert.exam.domain.QuestionExplanationRun;
import com.simulacert.exam.domain.QuestionOption;
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
    ExplanationResponse toExplanationResponse(QuestionExplanationRun question);

    @Mapping(target = "optionText", source = "optionText")
    @Mapping(target = "question", ignore = true)
    QuestionOption toQuestionOptionTranslate(QuestionOption opt, String optionText);
}
