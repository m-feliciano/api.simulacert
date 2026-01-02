package br.com.simulaaws.adapter.rest.controller;

import br.com.simulaaws.stats.application.dto.AttemptHistoryItemDto;
import br.com.simulaaws.stats.application.dto.AwsDomainStatsDto;
import br.com.simulaaws.stats.application.dto.UserStatsDto;
import br.com.simulaaws.stats.application.port.in.StatsUseCase;
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
public class StatsController {

    private final StatsUseCase statsUseCase;

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

    @GetMapping("/user/{userId}/domains")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AwsDomainStatsDto>> getPerformanceByDomain(@PathVariable UUID userId) {
        log.debug("Getting performance by AWS domain for user {}", userId);

        List<AwsDomainStatsDto> domainStats = statsUseCase.getPerformanceByAwsDomain(userId);

        return ResponseEntity.ok(domainStats);
    }
}

