package br.com.simulaaws.adapter.rest.controller;

import br.com.simulaaws.adapter.rest.controller.openapi.QuestionControllerOpenApi;
import br.com.simulaaws.exam.application.dto.CreateQuestionRequest;
import br.com.simulaaws.exam.application.dto.QuestionResponse;
import br.com.simulaaws.exam.application.port.in.QuestionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
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
    public ResponseEntity<List<QuestionResponse>> getQuestionsByExam(@PathVariable UUID examId) {
        log.debug("Getting questions for exam {}", examId);

        List<QuestionResponse> questions = questionUseCase.getQuestionsByExamId(examId);

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


