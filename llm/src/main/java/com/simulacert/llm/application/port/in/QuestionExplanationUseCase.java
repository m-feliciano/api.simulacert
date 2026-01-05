package com.simulacert.llm.application.port.in;

import com.simulacert.llm.application.dto.ExplanationResponse;
import com.simulacert.llm.application.dto.RequestExplanationCommand;
import com.simulacert.llm.application.dto.SubmitFeedbackCommand;

import java.util.UUID;

public interface QuestionExplanationUseCase {

    ExplanationResponse requestExplanation(RequestExplanationCommand command, UUID userId);

    void submitFeedback(UUID explanationId, SubmitFeedbackCommand command);
}

