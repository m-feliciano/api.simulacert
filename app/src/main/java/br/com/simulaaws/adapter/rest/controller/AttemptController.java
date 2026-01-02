package br.com.simulaaws.adapter.rest.controller;
}
    }
        return ResponseEntity.ok(attempts);

        List<AttemptResponse> attempts = attemptUseCase.getAttemptsByUser(userId);

        log.debug("Getting all attempts for user {}", userId);
    public ResponseEntity<List<AttemptResponse>> getAttemptsByUser(@PathVariable UUID userId) {
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/user/{userId}")

    }
        return ResponseEntity.ok(response);

        AttemptResponse response = attemptUseCase.getAttemptById(attemptId);

        log.debug("Getting attempt {}", attemptId);
    public ResponseEntity<AttemptResponse> getAttempt(@PathVariable UUID attemptId) {
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{attemptId}")

    }
        return ResponseEntity.ok(response);
        log.info("Attempt {} finished", attemptId);

        AttemptResponse response = attemptUseCase.finishAttempt(attemptId, score);

        log.info("Finishing attempt {} with score {}", attemptId, score);
            @RequestParam @Min(0) @Max(100) int score) {
            @PathVariable UUID attemptId,
    public ResponseEntity<AttemptResponse> finishAttempt(
    @PutMapping("/{attemptId}/finish")

    }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        log.info("Attempt started with id: {}", response.id());

        );
                request.questionCount()
                request.examId(),
                request.userId(),
        AttemptResponse response = attemptUseCase.startAttempt(

        log.info("Starting attempt for user {} on exam {}", request.userId(), request.examId());
    public ResponseEntity<AttemptResponse> startAttempt(@RequestBody StartAttemptRequest request) {
    @PreAuthorize("#request.userId() == authentication.principal.id")
    @PostMapping

    private final AttemptUseCase attemptUseCase;

public class AttemptController {
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/attempts")
@RestController
@Slf4j

import java.util.UUID;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import br.com.simulaaws.attempt.application.port.in.AttemptUseCase;
import br.com.simulaaws.attempt.application.dto.StartAttemptRequest;
import br.com.simulaaws.attempt.application.dto.AttemptResponse;


