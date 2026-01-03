package com.simulacert.attempt.application.port.out;

import java.util.List;
import java.util.UUID;

public interface AttemptQueryPort {

    List<UUID> findAttemptQuestions(UUID attemptId);

    long countCorrectAnswers(UUID attemptId);
}

