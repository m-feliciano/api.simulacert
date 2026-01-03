package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.controller.openapi.StatsControllerOpenApi;
import com.simulacert.stats.application.dto.AttemptHistoryItemDto;
import com.simulacert.stats.application.dto.AwsDomainStatsDto;
import com.simulacert.stats.application.dto.UserStatsDto;
import com.simulacert.stats.application.port.in.StatsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController implements StatsControllerOpenApi {

    private final StatsUseCase statsUseCase;

    @Override
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<UserStatsDto> getUserStatistics(@PathVariable UUID userId) {
        log.debug("Getting statistics for user {}", userId);

        UserStatsDto stats = statsUseCase.getUserStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/user/{userId}/history")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AttemptHistoryItemDto>> getAttemptHistory(@PathVariable UUID userId) {
        log.debug("Getting attempt history for user {}", userId);

        List<AttemptHistoryItemDto> history = statsUseCase.getAttemptHistory(userId);
        return ResponseEntity.ok(history);
    }

    @Override
    @GetMapping("/user/{userId}/domains")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AwsDomainStatsDto>> getPerformanceByDomain(@PathVariable UUID userId) {
        log.debug("Getting performance by AWS domain for user {}", userId);

        List<AwsDomainStatsDto> domainStats = statsUseCase.getPerformanceByAwsDomain(userId);
        return ResponseEntity.ok(domainStats);
    }
}

