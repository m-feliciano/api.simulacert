package br.com.simulaaws.llm.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearningAssistanceUseCase {

    Optional<String> requestExplanation(
        UUID questionId,
        UUID userAnswerId,
        UUID correctAnswerId
    );

    String getStudyRecommendations(UUID userId, List<String> weakDomains);
}

