package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.controller.openapi.QuestionControllerOpenApi;
import com.simulacert.exam.application.dto.request.CreateQuestionRequest;
import com.simulacert.exam.application.dto.response.QuestionResponse;
import com.simulacert.exam.application.port.in.QuestionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController implements QuestionControllerOpenApi {

    private final QuestionUseCase questionUseCase;

    @Override
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> getQuestion(@PathVariable UUID questionId) {
        log.debug("Getting question {}", questionId);

        QuestionResponse response = questionUseCase.getQuestionById(questionId);
        if (response == null) {
            log.warn("Question {} not found", questionId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/exam/{examId}")
    public ResponseEntity<Page<QuestionResponse>> getQuestionsByExam(
            @PathVariable UUID examId,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.debug("Getting paginated questions for exam {} with page: {}, size: {}",
                examId, pageable.getPageNumber(), pageable.getPageSize());

        Page<QuestionResponse> questions = questionUseCase.getQuestionsByExamIdPaginated(examId, pageable);

        return ResponseEntity.ok(questions);
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        log.info("Creating question for exam: {}", request.examId());

        QuestionResponse response = questionUseCase.createQuestion(request);
        log.info("Question created with id: {}", response.id());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
