package br.com.simulaaws.stats.infrastructure.persistence.mapper;

import br.com.simulaaws.stats.application.dto.AwsDomainStatsDto;
import br.com.simulaaws.stats.infrastructure.persistence.row.AwsDomainStatsRow;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AwsDomainStatsMapper {

    AwsDomainStatsDto toDto(AwsDomainStatsRow row);

    List<AwsDomainStatsDto> toDtoList(List<AwsDomainStatsRow> rows);
}

