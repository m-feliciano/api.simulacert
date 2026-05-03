package com.simulacert.review.infrastructure.persistence;

import com.simulacert.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByUserIdAndAttemptId(UUID userId, UUID attemptId);
    Optional<Review> findByUserIdAndAttemptId(UUID userId, UUID attemptId);
    long countByUserId(UUID userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.userId = :userId AND r.comment IS NOT NULL AND r.comment <> '' AND LENGTH(r.comment) >= 50")
    long countDetailedByUserId(@Param("userId") UUID userId);
}

