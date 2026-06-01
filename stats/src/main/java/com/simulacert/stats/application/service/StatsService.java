package com.simulacert.stats.application.service;

import com.simulacert.stats.application.dto.AttemptHistoryItemDto;
import com.simulacert.stats.application.dto.AwsDomainStatsDto;
import com.simulacert.stats.application.dto.UserStatsDto;
import com.simulacert.stats.application.port.in.StatsUseCase;
import com.simulacert.stats.application.port.out.StatsQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatsService implements StatsUseCase {
    private final StatsQueryPort statsQueryPort;

    @Override
    @Transactional(readOnly = true)
    public UserStatsDto getUserStatistics(UUID userId) {
        return statsQueryPort.getUserStats(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttemptHistoryItemDto> getAttemptHistory(UUID userId) {
        return statsQueryPort.getAttemptHistory(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AwsDomainStatsDto> getPerformanceByAwsDomain(UUID userId) {
        return statsQueryPort.getStatsByAwsDomain(userId);
    }
}
