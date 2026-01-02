package br.com.simulaaws.stats.infrastructure.persistence.mapper;

import br.com.simulaaws.stats.application.dto.AttemptHistoryItemDto;
import br.com.simulaaws.stats.infrastructure.persistence.row.AttemptHistoryRow;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AttemptHistoryMapper {

    AttemptHistoryItemDto toDto(AttemptHistoryRow row);

    List<AttemptHistoryItemDto> toDtoList(List<AttemptHistoryRow> rows);
}

