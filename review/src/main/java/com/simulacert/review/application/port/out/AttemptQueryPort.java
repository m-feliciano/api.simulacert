package com.simulacert.review.application.port.out;

import com.simulacert.attempt.application.dto.AttemptVo;

import java.util.Optional;
import java.util.UUID;

public interface AttemptQueryPort {
    Optional<AttemptVo> findById(UUID attemptId);
}

