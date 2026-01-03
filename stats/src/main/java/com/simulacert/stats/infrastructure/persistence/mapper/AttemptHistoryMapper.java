package com.simulacert.stats.infrastructure.persistence.mapper;

import com.simulacert.stats.application.dto.AttemptHistoryItemDto;
import com.simulacert.stats.infrastructure.persistence.row.AttemptHistoryRow;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AttemptHistoryMapper {

    AttemptHistoryItemDto toDto(AttemptHistoryRow row);

    List<AttemptHistoryItemDto> toDtoList(List<AttemptHistoryRow> rows);
}

