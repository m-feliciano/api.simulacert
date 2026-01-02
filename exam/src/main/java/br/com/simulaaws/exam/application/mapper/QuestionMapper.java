package br.com.simulaaws.exam.application.mapper;

import br.com.simulaaws.exam.application.dto.response.QuestionResponse;
import br.com.simulaaws.exam.domain.Question;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QuestionMapper {

    QuestionResponse toResponse(Question question);

    List<QuestionResponse> toResponseList(List<Question> questions);
}
