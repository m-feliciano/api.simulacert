package br.com.simulaaws.attempt.application.mapper;

import br.com.simulaaws.attempt.application.dto.AttemptResponse;
import br.com.simulaaws.attempt.domain.Attempt;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AttemptMapper {

    AttemptResponse toResponse(Attempt domain);
}