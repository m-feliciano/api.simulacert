package com.simulacert.stats.infrastructure.persistence.mapper;

import com.simulacert.stats.application.dto.UserStatsDto;
import com.simulacert.stats.infrastructure.persistence.row.UserStatsRow;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface UserStatsMapper {

    UserStatsDto toDto(UserStatsRow row);
}
