package br.com.simulaaws.stats.application.port.in;

import br.com.simulaaws.stats.application.dto.AttemptHistoryItemDto;
import br.com.simulaaws.stats.application.dto.AwsDomainStatsDto;
import br.com.simulaaws.stats.application.dto.UserStatsDto;

import java.util.List;
import java.util.UUID;

public interface StatsUseCase {
    UserStatsDto getUserStatistics(UUID userId);

    List<AttemptHistoryItemDto> getAttemptHistory(UUID userId);

    List<AwsDomainStatsDto> getPerformanceByAwsDomain(UUID userId);
}
