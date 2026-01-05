package com.simulacert.review.application.port.in;

import com.simulacert.review.application.dto.CreateReviewRequest;
import com.simulacert.review.application.dto.ReviewResponse;

import java.util.Optional;
import java.util.UUID;

public interface CreateReviewUseCase {
    ReviewResponse createReview(UUID userId, CreateReviewRequest request);
    Optional<ReviewResponse> getReviewByAttempt(UUID userId, UUID attemptId);
}

