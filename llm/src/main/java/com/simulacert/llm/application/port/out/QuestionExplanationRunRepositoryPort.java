package com.simulacert.llm.application.port.out;

import com.simulacert.llm.domain.QuestionExplanationRun;

import java.util.Optional;
import java.util.UUID;

public interface QuestionExplanationRunRepositoryPort {

    QuestionExplanationRun save(QuestionExplanationRun explanationRun);

    Optional<QuestionExplanationRun> findById(UUID id);

    Optional<QuestionExplanationRun> findByQuestionIdAndLanguage(UUID questionId, String language);
}