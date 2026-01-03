package com.simulacert.attempt.application.port.in;

import com.simulacert.attempt.application.dto.AttemptQuestionResponse;
import com.simulacert.attempt.application.dto.AttemptVo;

import java.util.List;
import java.util.UUID;

public interface AttemptUseCase {

    AttemptVo startAttempt(UUID userId, UUID examId, int questionCount);

    AttemptVo finishAttempt(UUID attemptId);

    AttemptVo getAttemptById(UUID attemptId);

    List<AttemptVo> getAttemptsByUser(UUID userId);

    List<AttemptQuestionResponse> getAttemptQuestions(UUID attemptId);
}
