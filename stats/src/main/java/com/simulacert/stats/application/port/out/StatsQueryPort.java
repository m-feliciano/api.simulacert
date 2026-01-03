package com.simulacert.stats.application.port.out;

import com.simulacert.stats.application.dto.AttemptHistoryItemDto;
import com.simulacert.stats.application.dto.AwsDomainStatsDto;
import com.simulacert.stats.application.dto.UserStatsDto;

import java.util.List;
import java.util.UUID;

public interface StatsQueryPort {
    UserStatsDto getUserStats(UUID userId);

    List<AttemptHistoryItemDto> getAttemptHistory(UUID userId);

    List<AwsDomainStatsDto> getStatsByAwsDomain(UUID userId);
}
