package com.simulacert.exam.infrastructure.persistence.adapter;

import com.simulacert.common.ClockPort;
import com.simulacert.exam.application.port.out.QuestionExplanationRunRepositoryPort;
import com.simulacert.exam.domain.QuestionExplanationRun;
import com.simulacert.exam.infrastructure.persistence.repository.QuestionExplanationRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class QuestionExplanationRunRepositoryAdapter implements QuestionExplanationRunRepositoryPort {

    private final QuestionExplanationRunRepository repository;
    private final ClockPort clock;

    @Override
    public QuestionExplanationRun save(QuestionExplanationRun explanationRun) {
        return repository.save(explanationRun);
    }

    @Override
    public Optional<QuestionExplanationRun> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<List<QuestionExplanationRun>> findByQuestionIdAndLanguage(UUID questionId, String language) {
        return repository.findByQuestionIdAndLanguage(questionId, language, clock.now());
    }

    @Override
    public List<QuestionExplanationRun> findByQuestionIdsAndExamIdAndLanguage(List<UUID> uuids, String language) {
        return repository.findByQuestionIdsAndExamId(uuids, language, clock.now());
    }
}