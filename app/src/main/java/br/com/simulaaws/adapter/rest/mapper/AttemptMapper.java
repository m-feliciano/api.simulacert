package br.com.simulaaws.adapter.rest.mapper;

import br.com.simulaaws.adapter.rest.dto.AttemptResponse;
import br.com.simulaaws.attempt.application.dto.AttemptVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AttemptMapper {

    AttemptResponse toResponse(AttemptVo attemptVo);

    List<AttemptResponse> toResponseList(List<AttemptVo> attempts);
}