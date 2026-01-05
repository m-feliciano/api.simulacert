package com.simulacert.review.application.port.out;

import com.simulacert.review.domain.Review;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepositoryPort {
    Review save(Review review);
    boolean existsByUserIdAndAttemptId(UUID userId, UUID attemptId);
    Optional<Review> findByUserIdAndAttemptId(UUID userId, UUID attemptId);
}

