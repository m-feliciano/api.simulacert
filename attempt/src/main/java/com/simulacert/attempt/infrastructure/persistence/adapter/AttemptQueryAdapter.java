package com.simulacert.attempt.infrastructure.persistence.adapter;

import com.simulacert.attempt.application.port.out.AttemptQueryPort;
import com.simulacert.attempt.domain.Attempt;
import com.simulacert.attempt.infrastructure.persistence.repository.AnswerRepository;
import com.simulacert.attempt.infrastructure.persistence.repository.AttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttemptQueryAdapter implements AttemptQueryPort {

    private final AttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;

    @Override
    public List<UUID> findAttemptQuestions(UUID attemptId) {
        return attemptRepository.findById(attemptId)
                .map(Attempt::getQuestionIds)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));
    }

    @Override
    public long countCorrectAnswers(UUID attemptId) {
        return answerRepository.countCorrectAnswersByAttemptId(attemptId);
    }
}

