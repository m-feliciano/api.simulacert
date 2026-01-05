package com.simulacert.review.infrastructure.persistence;

import com.simulacert.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByUserIdAndAttemptId(UUID userId, UUID attemptId);
    Optional<Review> findByUserIdAndAttemptId(UUID userId, UUID attemptId);
}

