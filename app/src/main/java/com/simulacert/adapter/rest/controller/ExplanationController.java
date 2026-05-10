package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.controller.openapi.ExplanationControllerOpenApi;
import com.simulacert.exam.application.dto.request.RequestExplanationCommand;
import com.simulacert.exam.application.port.in.QuestionExplanationUseCase;
import com.simulacert.llm.application.dto.ExplanationResponse;
import com.simulacert.llm.application.dto.SubmitFeedbackCommand;
import com.simulacert.util.UserContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class ExplanationController implements ExplanationControllerOpenApi {

    private final QuestionExplanationUseCase explanationUseCase;

    @Override
    @PostMapping("/questions/{questionId}/explanations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExplanationResponse> requestExplanation(
            @PathVariable UUID questionId,
            @RequestBody @Valid RequestExplanationCommand command
    ) {
        if (!questionId.equals(command.questionId())) {
            throw new IllegalArgumentException("Question ID in path does not match request body");
        }

        ExplanationResponse response = explanationUseCase.requestExplanation(command, UserContextHolder.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PostMapping("/explanations/{explanationId}/feedback")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitFeedback(
            @RequestBody @Valid SubmitFeedbackCommand command,
            @PathVariable UUID explanationId
    ) {
        explanationUseCase.submitFeedback(explanationId, command);
    }
}

