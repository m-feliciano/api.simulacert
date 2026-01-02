package br.com.simulaaws.attempt.infrastructure.persistence.adapter;

import br.com.simulaaws.attempt.application.port.out.AnswerRepositoryPort;
import br.com.simulaaws.attempt.domain.Answer;
import br.com.simulaaws.attempt.infrastructure.persistence.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnswerRepositoryAdapter implements AnswerRepositoryPort {

    private final AnswerRepository repository;

    @Override
    public Answer save(Answer answer) {
        return repository.save(answer);
    }

    @Override
    public boolean existsByAttemptIdAndQuestionId(UUID attemptId, UUID questionId) {
        return repository.existsByAttemptIdAndQuestionId(attemptId, questionId);
    }

    @Override
    public List<Answer> findByAttemptId(UUID attemptId) {
        return repository.findByAttemptId(attemptId);
    }

    @Override
    public void deleteByAttemptIdAndQuestionId(UUID attemptId, UUID questionId) {
        repository.deleteByAttemptIdAndQuestionId(attemptId, questionId);
    }
}

