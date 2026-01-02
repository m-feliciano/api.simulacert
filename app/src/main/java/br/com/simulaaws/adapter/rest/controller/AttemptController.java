package br.com.simulaaws.adapter.rest.controller;

import br.com.simulaaws.adapter.rest.controller.openapi.AttemptControllerOpenApi;
import br.com.simulaaws.adapter.rest.dto.AttemptResponse;
import br.com.simulaaws.adapter.rest.dto.StartAttemptRequest;
import br.com.simulaaws.adapter.rest.mapper.AttemptMapper;
import br.com.simulaaws.attempt.application.dto.AttemptVo;
import br.com.simulaaws.attempt.application.port.in.AttemptUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/attempts")
@RequiredArgsConstructor
@Validated
public class AttemptController implements AttemptControllerOpenApi {

    private final AttemptUseCase useCase;
    private final AttemptMapper mapper;

    @Override
    @PostMapping
    @PreAuthorize("#request.userId() == authentication.principal.id")
    public ResponseEntity<Void> startAttempt(@RequestBody StartAttemptRequest request) {
        log.info("Starting attempt for user {} on exam {}", request.userId(), request.examId());

        AttemptVo response = useCase.startAttempt(
                request.userId(),
                request.examId(),
                request.questionCount()
        );

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        log.info("Attempt started with id: {}", response.id());
        return ResponseEntity.created(uri).build();
    }

    @Override
    @PutMapping("/{attemptId}/finish")
    public ResponseEntity<AttemptResponse> finishAttempt(
            @PathVariable UUID attemptId,
            @RequestParam @Min(0) @Max(100) int score) {
        log.info("Finishing attempt {} with score {}", attemptId, score);

        AttemptVo response = useCase.finishAttempt(attemptId, score);

        log.info("Attempt {} finished", attemptId);
        return ResponseEntity.ok(mapper.toResponse(response));
    }

    @Override
    @GetMapping("/{attemptId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttemptResponse> getAttempt(@PathVariable UUID attemptId) {
        log.debug("Getting attempt {}", attemptId);

        AttemptVo response = useCase.getAttemptById(attemptId);

        return ResponseEntity.ok(mapper.toResponse(response));
    }

    @Override
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AttemptResponse>> getAttemptsByUser(@PathVariable UUID userId) {
        log.debug("Getting all attempts for user {}", userId);

        List<AttemptVo> attempts = useCase.getAttemptsByUser(userId);

        return ResponseEntity.ok(mapper.toResponseList(attempts));
    }
}
