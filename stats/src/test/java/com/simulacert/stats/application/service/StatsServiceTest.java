package com.simulacert.stats.application.service;

import com.simulacert.stats.application.dto.AttemptHistoryItemDto;
import com.simulacert.stats.application.dto.AwsDomainStatsDto;
import com.simulacert.stats.application.dto.UserStatsDto;
import com.simulacert.stats.application.port.out.StatsQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatsService Tests")
class StatsServiceTest {

    @Mock
    private StatsQueryPort statsQueryPort;

    @InjectMocks
    private StatsService statsService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should get user statistics successfully")
    void shouldGetUserStatisticsSuccessfully() {
        UserStatsDto expectedStats = new UserStatsDto(
                userId,
                10,
                7,
                75.5,
                850,
                Instant.parse("2026-01-01T10:00:00Z")
        );

        when(statsQueryPort.getUserStats(userId)).thenReturn(expectedStats);

        UserStatsDto result = statsService.getUserStatistics(userId);

        assertThat(result).isNotNull();
        assertThat(result.totalAttempts()).isEqualTo(10);
        assertThat(result.completedAttempts()).isEqualTo(7);
        assertThat(result.averageScore()).isEqualTo(75.5);
        assertThat(result.bestScore()).isEqualTo(850);
        verify(statsQueryPort).getUserStats(userId);
    }

    @Test
    @DisplayName("Should get attempt history successfully")
    void shouldGetAttemptHistorySuccessfully() {
        Instant now = Instant.parse("2026-01-04T10:00:00Z");
        UUID examId1 = UUID.randomUUID();
        UUID examId2 = UUID.randomUUID();

        List<AttemptHistoryItemDto> expectedHistory = List.of(
                new AttemptHistoryItemDto(
                        UUID.randomUUID(),
                        examId1,
                        "AWS Practice Exam 1",
                        now.minusSeconds(3600),
                        now,
                        85,
                        "COMPLETED"
                ),
                new AttemptHistoryItemDto(
                        UUID.randomUUID(),
                        examId2,
                        "AWS Practice Exam 2",
                        now.minusSeconds(7200),
                        now.minusSeconds(5400),
                        72,
                        "COMPLETED"
                )
        );

        when(statsQueryPort.getAttemptHistory(userId)).thenReturn(expectedHistory);

        List<AttemptHistoryItemDto> result = statsService.getAttemptHistory(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).score()).isEqualTo(85);
        assertThat(result.get(1).score()).isEqualTo(72);
        verify(statsQueryPort).getAttemptHistory(userId);
    }

    @Test
    @DisplayName("Should get performance by AWS domain successfully")
    void shouldGetPerformanceByAwsDomainSuccessfully() {
        List<AwsDomainStatsDto> expectedStats = List.of(
                new AwsDomainStatsDto("AWS", 100, 85, 85.0),
                new AwsDomainStatsDto("Compute", 50, 42, 84.0),
                new AwsDomainStatsDto("Storage", 30, 24, 80.0)
        );

        when(statsQueryPort.getStatsByAwsDomain(userId)).thenReturn(expectedStats);

        List<AwsDomainStatsDto> result = statsService.getPerformanceByAwsDomain(userId);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).awsDomain()).isEqualTo("AWS");
        assertThat(result.get(0).accuracyRate()).isEqualTo(85.0);
        assertThat(result.get(1).awsDomain()).isEqualTo("Compute");
        assertThat(result.get(2).awsDomain()).isEqualTo("Storage");
        verify(statsQueryPort).getStatsByAwsDomain(userId);
    }

    @Test
    @DisplayName("Should return empty list when user has no attempts")
    void shouldReturnEmptyListWhenUserHasNoAttempts() {
        when(statsQueryPort.getAttemptHistory(userId)).thenReturn(List.of());

        List<AttemptHistoryItemDto> result = statsService.getAttemptHistory(userId);

        assertThat(result).isEmpty();
        verify(statsQueryPort).getAttemptHistory(userId);
    }

    @Test
    @DisplayName("Should return empty list when user has no domain stats")
    void shouldReturnEmptyListWhenUserHasNoDomainStats() {
        when(statsQueryPort.getStatsByAwsDomain(userId)).thenReturn(List.of());

        List<AwsDomainStatsDto> result = statsService.getPerformanceByAwsDomain(userId);

        assertThat(result).isEmpty();
        verify(statsQueryPort).getStatsByAwsDomain(userId);
    }

    @Test
    @DisplayName("Should get user statistics with zero attempts")
    void shouldGetUserStatisticsWithZeroAttempts() {
        UserStatsDto expectedStats = new UserStatsDto(
                userId,
                0,
                0,
                0.0,
                null,
                null
        );

        when(statsQueryPort.getUserStats(userId)).thenReturn(expectedStats);

        UserStatsDto result = statsService.getUserStatistics(userId);

        assertThat(result).isNotNull();
        assertThat(result.totalAttempts()).isZero();
        assertThat(result.completedAttempts()).isZero();
        assertThat(result.averageScore()).isZero();
        verify(statsQueryPort).getUserStats(userId);
    }
}

