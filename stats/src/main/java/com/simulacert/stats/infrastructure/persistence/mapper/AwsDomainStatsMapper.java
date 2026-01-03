package com.simulacert.stats.infrastructure.persistence.mapper;

import com.simulacert.stats.application.dto.AwsDomainStatsDto;
import com.simulacert.stats.infrastructure.persistence.row.AwsDomainStatsRow;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AwsDomainStatsMapper {

    AwsDomainStatsDto toDto(AwsDomainStatsRow row);

    List<AwsDomainStatsDto> toDtoList(List<AwsDomainStatsRow> rows);
}

