package com.simulacert.review.infrastructure.persistence;

import com.simulacert.review.application.port.out.ReviewRepositoryPort;
import com.simulacert.review.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepositoryPort {

    private final JpaReviewRepository jpaRepository;

    @Override
    public Review save(Review review) {
        return jpaRepository.save(review);
    }

    @Override
    public boolean existsByUserIdAndAttemptId(UUID userId, UUID attemptId) {
        return jpaRepository.existsByUserIdAndAttemptId(userId, attemptId);
    }

    @Override
    public Optional<Review> findByUserIdAndAttemptId(UUID userId, UUID attemptId) {
        return jpaRepository.findByUserIdAndAttemptId(userId, attemptId);
    }
}

