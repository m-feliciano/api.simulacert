package br.com.simulaaws.attempt.application.port.in;

import br.com.simulaaws.attempt.application.dto.AttemptResponse;

import java.util.List;
import java.util.UUID;

public interface AttemptUseCase {

    AttemptResponse startAttempt(UUID userId, UUID examId, int questionCount);

    AttemptResponse finishAttempt(UUID attemptId, int score);

    AttemptResponse getAttemptById(UUID attemptId);

    List<AttemptResponse> getAttemptsByUser(UUID userId);
}
