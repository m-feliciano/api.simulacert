package com.simulacert.exam.application.port.out;

import com.simulacert.exam.domain.QuestionExplanationRun;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionExplanationRunRepositoryPort {

    QuestionExplanationRun save(QuestionExplanationRun explanationRun);

    Optional<QuestionExplanationRun> findById(UUID id);

    Optional<List<QuestionExplanationRun>> findAllByQuestion(UUID questionId);

    int countExplanationsByUserIdToday(UUID userId);
}