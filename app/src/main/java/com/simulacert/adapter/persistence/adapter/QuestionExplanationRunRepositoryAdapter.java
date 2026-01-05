package com.simulacert.adapter.persistence.adapter;

import com.simulacert.adapter.persistence.repository.QuestionExplanationRunRepository;
import com.simulacert.common.ClockPort;
import com.simulacert.llm.application.port.out.QuestionExplanationRunRepositoryPort;
import com.simulacert.llm.domain.QuestionExplanationRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}