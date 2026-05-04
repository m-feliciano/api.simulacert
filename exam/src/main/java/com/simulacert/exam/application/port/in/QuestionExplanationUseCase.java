package com.simulacert.exam.application.port.in;

import com.simulacert.exam.application.dto.request.RequestExplanationCommand;
import com.simulacert.llm.application.dto.ExplanationResponse;
import com.simulacert.llm.application.dto.SubmitFeedbackCommand;

import java.util.List;
import java.util.UUID;

public interface QuestionExplanationUseCase {

    ExplanationResponse requestExplanation(RequestExplanationCommand command, UUID userId);

    void submitFeedback(UUID explanationId, SubmitFeedbackCommand command);

    List<ExplanationResponse> getExplanationsForQuestions(List<UUID> questionIds, String language);
}

