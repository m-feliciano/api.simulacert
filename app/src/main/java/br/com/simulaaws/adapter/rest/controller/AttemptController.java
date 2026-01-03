package br.com.simulaaws.adapter.rest.controller;

import br.com.simulaaws.adapter.rest.controller.openapi.AttemptControllerOpenApi;
import br.com.simulaaws.adapter.rest.dto.AttemptResponse;
import br.com.simulaaws.adapter.rest.dto.StartAttemptRequest;
import br.com.simulaaws.adapter.rest.mapper.AttemptMapper;
import br.com.simulaaws.attempt.application.dto.AttemptQuestionResponse;
import br.com.simulaaws.attempt.application.dto.AttemptVo;
import br.com.simulaaws.attempt.application.dto.SubmitAnswerRequest;
import br.com.simulaaws.attempt.application.port.in.AnswerUseCase;
import br.com.simulaaws.attempt.application.port.in.AttemptUseCase;
import lombok.Delegate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<AttemptResponse> startAttempt(@RequestBody StartAttemptRequest request) {
        log.info("Starting attempt for user {} on exam {}", request.userId(), request.examId());

        AttemptVo attemptVo = useCase.startAttempt(
                request.userId(),
                request.examId(),
                request.questionCount()
        );

        log.info("Attempt started with id: {}", attemptVo.id());
        AttemptResponse response = mapper.toResponse(attemptVo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PostMapping("/{attemptId}/finish")
    public ResponseEntity<AttemptResponse> finishAttempt(@PathVariable UUID attemptId) {
        log.info("Finishing attempt {}", attemptId);

        AttemptVo response = useCase.finishAttempt(attemptId);

        log.info("Attempt {} finished with score {}", attemptId, response.score());
        return ResponseEntity.ok(mapper.toResponse(response));
    }

    @Override
    @GetMapping("/{attemptId}")
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

    @Override
    @GetMapping("/{attemptId}/questions")
    public ResponseEntity<List<AttemptQuestionResponse>> getAttemptQuestions(@PathVariable UUID attemptId) {
        log.debug("Getting questions for attempt {}", attemptId);

        List<AttemptQuestionResponse> questions = useCase.getAttemptQuestions(attemptId);

        return ResponseEntity.ok(questions);
    }

    @Override
    @PostMapping("/{attemptId}/answers/{questionId}")
    public ResponseEntity<Void> submitAnswer(
            @PathVariable UUID attemptId,
            @PathVariable UUID questionId,
            @RequestBody SubmitAnswerRequest request) {
        log.info("Submitting answer for attempt {} question {}", attemptId, questionId);

        answerUseCase.submitAnswer(attemptId, questionId, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @DeleteMapping("/{attemptId}/answers/{questionId}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable UUID attemptId,
            @PathVariable UUID questionId) {
        log.info("Deleting answer for attempt {} question {}", attemptId, questionId);

        answerUseCase.deleteAnswer(attemptId, questionId);

        return ResponseEntity.noContent().build();
    }
}

