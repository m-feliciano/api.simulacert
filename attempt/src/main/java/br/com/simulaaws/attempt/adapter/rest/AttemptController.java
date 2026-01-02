package br.com.simulaaws.attempt.adapter.rest;

import br.com.simulaaws.attempt.adapter.rest.openapi.AttemptControllerOpenApi;
import br.com.simulaaws.attempt.application.dto.AttemptResponse;
import br.com.simulaaws.attempt.application.dto.StartAttemptRequest;
import br.com.simulaaws.attempt.application.port.in.AttemptUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/attempts")
@RequiredArgsConstructor
@Validated
public class AttemptController implements AttemptControllerOpenApi {

    private final AttemptUseCase attemptUseCase;

    @Override
    @PostMapping
    @PreAuthorize("#request.userId() == authentication.principal.id")
    public ResponseEntity<AttemptResponse> startAttempt(@RequestBody StartAttemptRequest request) {
        log.info("Starting attempt for user {} on exam {}", request.userId(), request.examId());

        AttemptResponse response = attemptUseCase.startAttempt(
                request.userId(),
                request.examId(),
                request.questionCount()
        );

        log.info("Attempt started with id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PutMapping("/{attemptId}/finish")
    public ResponseEntity<AttemptResponse> finishAttempt(
            @PathVariable UUID attemptId,
            @RequestParam @Min(0) @Max(100) int score) {
        log.info("Finishing attempt {} with score {}", attemptId, score);


        AttemptResponse response = attemptUseCase.finishAttempt(attemptId, score);

        log.info("Attempt {} finished", attemptId);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{attemptId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttemptResponse> getAttempt(@PathVariable UUID attemptId) {
        log.debug("Getting attempt {}", attemptId);

        AttemptResponse response = attemptUseCase.getAttemptById(attemptId);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AttemptResponse>> getAttemptsByUser(@PathVariable UUID userId) {
        log.debug("Getting all attempts for user {}", userId);

        List<AttemptResponse> attempts = attemptUseCase.getAttemptsByUser(userId);

        return ResponseEntity.ok(attempts);
    }
}
