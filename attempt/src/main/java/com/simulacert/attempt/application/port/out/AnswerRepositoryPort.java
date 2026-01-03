package com.simulacert.attempt.application.port.out;

import com.simulacert.attempt.domain.Answer;

import java.util.List;
import java.util.UUID;

public interface AnswerRepositoryPort {

    Answer save(Answer answer);

    boolean existsByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);

    List<Answer> findByAttemptId(UUID attemptId);

    void deleteByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);
}

