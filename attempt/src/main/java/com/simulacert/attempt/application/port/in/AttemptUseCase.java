package com.simulacert.attempt.application.port.in;

import com.simulacert.attempt.application.dto.AttemptQuestionResponse;
import com.simulacert.attempt.application.dto.AttemptResponse;
import com.simulacert.attempt.application.dto.AttemptTimingResponse;
import com.simulacert.attempt.application.dto.AttemptVo;
import com.simulacert.attempt.application.dto.StartAttemptRequest;

import java.util.List;
import java.util.UUID;

public interface AttemptUseCase {

    AttemptVo startAttempt(StartAttemptRequest startAttemptRequest);

    AttemptVo finishAttempt(UUID attemptId);

    void cancelAttempt(UUID attemptId);

    AttemptTimingResponse pauseAttempt(UUID attemptId);

    AttemptTimingResponse resumeAttempt(UUID attemptId);

    AttemptTimingResponse heartbeatAttempt(UUID attemptId);

    AttemptVo getAttemptById(UUID attemptId);

    List<AttemptVo> getAttemptsByUser(UUID userId);

    List<AttemptQuestionResponse> getAttemptQuestions(UUID attemptId, String language);

    AttemptResponse retakeAttempt(UUID attemptId);
}
