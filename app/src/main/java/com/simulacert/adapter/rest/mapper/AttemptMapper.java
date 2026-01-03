package com.simulacert.adapter.rest.mapper;

import com.simulacert.adapter.rest.dto.AttemptResponse;
import com.simulacert.attempt.application.dto.AttemptVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AttemptMapper {

    AttemptResponse toResponse(AttemptVo attemptVo);

    List<AttemptResponse> toResponseList(List<AttemptVo> attempts);
}