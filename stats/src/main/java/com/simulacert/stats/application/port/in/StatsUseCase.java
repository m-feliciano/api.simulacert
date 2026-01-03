package com.simulacert.stats.application.port.in;

import com.simulacert.stats.application.dto.AttemptHistoryItemDto;
import com.simulacert.stats.application.dto.AwsDomainStatsDto;
import com.simulacert.stats.application.dto.UserStatsDto;

import java.util.List;
import java.util.UUID;

public interface StatsUseCase {
    UserStatsDto getUserStatistics(UUID userId);

    List<AttemptHistoryItemDto> getAttemptHistory(UUID userId);

    List<AwsDomainStatsDto> getPerformanceByAwsDomain(UUID userId);
}
