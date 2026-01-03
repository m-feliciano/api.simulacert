package com.simulacert.llm.application.port.out;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface LearningAssistantPort {
    boolean isAvailable();
    String generateStudyTips(UUID userId, List<String> weakDomains);
    Optional<String> explainIncorrectAnswer(UUID questionId, UUID userAnswerId, UUID correctAnswerId);
}
