package com.simulacert.attempt.application.port.in;

import com.simulacert.attempt.application.dto.SubmitAnswerRequest;

import java.util.UUID;

public interface AnswerUseCase {

    void submitAnswer(UUID attemptId, UUID questionId, SubmitAnswerRequest request);

    void deleteAnswer(UUID attemptId, UUID questionId);
}