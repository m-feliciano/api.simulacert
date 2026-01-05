package com.simulacert.review.infrastructure.persistence;

import com.simulacert.attempt.application.dto.AttemptVo;
import com.simulacert.attempt.application.port.in.AttemptUseCase;
import com.simulacert.review.application.port.out.AttemptQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("reviewAttemptQueryAdapter")
@RequiredArgsConstructor
public class ReviewAttemptQueryAdapter implements AttemptQueryPort {

    private final AttemptUseCase attemptUseCase;

    @Override
    public Optional<AttemptVo> findById(UUID attemptId) {
        try {
            AttemptVo attempt = attemptUseCase.getAttemptById(attemptId);
            return Optional.of(attempt);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
