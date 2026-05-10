package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.controller.openapi.AttemptControllerOpenApi;
import com.simulacert.attempt.application.dto.AnswerResponse;
import com.simulacert.attempt.application.dto.AttemptQuestionResponse;
import com.simulacert.attempt.application.dto.AttemptResponse;
import com.simulacert.attempt.application.dto.AttemptTimingResponse;
import com.simulacert.attempt.application.dto.AttemptVo;
import com.simulacert.attempt.application.dto.StartAttemptRequest;
import com.simulacert.attempt.application.dto.SubmitAnswerRequest;
import com.simulacert.attempt.application.port.in.AnswerUseCase;
import com.simulacert.attempt.application.port.in.AttemptUseCase;
import com.simulacert.attempt.mapper.AttemptMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    private final AnswerUseCase answerUseCase;
    private final AttemptMapper mapper;

    @Override
    @PostMapping
    @PreAuthorize("#request.userId() == authentication.principal.id")
    public ResponseEntity<Void> startAttempt(@RequestBody @Valid StartAttemptRequest request) {
        AttemptVo attemptVo = useCase.startAttempt(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(attemptVo.id())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Override
    @PostMapping("/{attemptId}/finish")
    public ResponseEntity<AttemptResponse> finishAttempt(@PathVariable UUID attemptId) {
        AttemptVo response = useCase.finishAttempt(attemptId);
        return ResponseEntity.ok(mapper.toResponse(response));
    }

    @Override
    @GetMapping("/{attemptId}")
    public ResponseEntity<AttemptResponse> getAttempt(@PathVariable UUID attemptId) {
        AttemptVo response = useCase.getAttemptById(attemptId);
        return ResponseEntity.ok(mapper.toResponse(response));
    }

    @Override
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AttemptResponse>> getAttemptsByUser(@PathVariable UUID userId) {
        List<AttemptVo> attempts = useCase.getAttemptsByUser(userId);
        return ResponseEntity.ok(mapper.toResponseList(attempts));
    }

    @Override
    @GetMapping("/{attemptId}/questions")
    public ResponseEntity<List<AttemptQuestionResponse>> getAttemptQuestions(@PathVariable UUID attemptId) {
        return ResponseEntity.ok(useCase.getAttemptQuestions(attemptId));
    }

    @Override
    @PostMapping("/{attemptId}/answers/{questionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void submitAnswer(
            @PathVariable UUID attemptId,
            @PathVariable UUID questionId,
            @RequestBody @Valid SubmitAnswerRequest request) {
        answerUseCase.submitAnswer(attemptId, questionId, request);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{attemptId}/answers")
    public List<AnswerResponse> getAnswer(@PathVariable UUID attemptId) {
        return answerUseCase.getAnswer(attemptId);
    }

    @Override
    @DeleteMapping("/{attemptId}/answers/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnswer(
            @PathVariable UUID attemptId,
            @PathVariable UUID questionId) {
        answerUseCase.deleteAnswer(attemptId, questionId);
    }

    @Override
    @PostMapping("/{attemptId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelAttempt(@PathVariable UUID attemptId) {
        useCase.cancelAttempt(attemptId);
    }

    @Override
    @PostMapping("/{attemptId}/pause")
    public ResponseEntity<AttemptTimingResponse> pauseAttempt(@PathVariable UUID attemptId) {
        return ResponseEntity.ok(useCase.pauseAttempt(attemptId));
    }

    @Override
    @PostMapping("/{attemptId}/resume")
    public ResponseEntity<AttemptTimingResponse> resumeAttempt(@PathVariable UUID attemptId) {
        return ResponseEntity.ok(useCase.resumeAttempt(attemptId));
    }

    @Override
    @PostMapping("/{attemptId}/heartbeat")
    public ResponseEntity<AttemptTimingResponse> heartbeatAttempt(@PathVariable UUID attemptId) {
        return ResponseEntity.ok(useCase.heartbeatAttempt(attemptId));
    }

    @Override
    @PostMapping("/{attemptId}/retake")
    @ResponseStatus(HttpStatus.OK)
    @PostAuthorize("returnObject.userId() == authentication.principal.id or hasRole('ADMIN')")
    public AttemptResponse retakeAttempt(@PathVariable UUID attemptId) {
        return useCase.retakeAttempt(attemptId);
    }
}

