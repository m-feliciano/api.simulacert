package br.com.simulaaws.attempt.application.port.out;

import br.com.simulaaws.attempt.domain.Answer;

import java.util.List;
import java.util.UUID;

public interface AnswerRepositoryPort {

    Answer save(Answer answer);

    boolean existsByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);

    List<Answer> findByAttemptId(UUID attemptId);

    void deleteByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);
}

