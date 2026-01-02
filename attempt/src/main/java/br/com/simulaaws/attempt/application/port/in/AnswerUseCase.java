package br.com.simulaaws.attempt.application.port.in;

import br.com.simulaaws.attempt.application.dto.SubmitAnswerRequest;

import java.util.UUID;

public interface AnswerUseCase {

    void submitAnswer(UUID attemptId, UUID questionId, SubmitAnswerRequest request);
}