package br.com.simulaaws.stats.infrastructure.persistence.mapper;

import br.com.simulaaws.stats.application.dto.UserStatsDto;
import br.com.simulaaws.stats.infrastructure.persistence.row.UserStatsRow;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface UserStatsMapper {

    UserStatsDto toDto(UserStatsRow row);
}
