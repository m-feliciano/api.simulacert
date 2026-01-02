package br.com.simulaaws.attempt.infrastructure.persistence.adapter;

import br.com.simulaaws.attempt.application.port.out.AttemptQueryPort;
import br.com.simulaaws.attempt.domain.Attempt;
import br.com.simulaaws.attempt.infrastructure.persistence.repository.AnswerRepository;
import br.com.simulaaws.attempt.infrastructure.persistence.repository.AttemptRepository;
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

