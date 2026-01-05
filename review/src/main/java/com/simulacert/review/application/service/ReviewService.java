package com.simulacert.review.application.service;

import com.simulacert.common.ClockPort;
import com.simulacert.review.application.dto.CreateReviewRequest;
import com.simulacert.review.application.dto.ReviewResponse;
import com.simulacert.review.application.port.in.CreateReviewUseCase;
import com.simulacert.review.application.port.out.AttemptQueryPort;
import com.simulacert.review.application.port.out.ReviewRepositoryPort;
import com.simulacert.review.domain.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService implements CreateReviewUseCase {

    private final ReviewRepositoryPort reviewRepository;
    private final AttemptQueryPort attemptQueryPort;
    private final ClockPort clock;

    @Override
    @Transactional
    public ReviewResponse createReview(UUID userId, CreateReviewRequest request) {
        log.info("Creating review for attempt {} by user {}", request.attemptId(), userId);

        var attempt = attemptQueryPort.findById(request.attemptId())
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + request.attemptId()));

        if (!attempt.userId().equals(userId)) {
            log.warn("User {} tried to review attempt {} that belongs to user {}", userId, request.attemptId(), attempt.userId());
            throw new IllegalArgumentException("You can only review your own attempts");
        }

        if (!"COMPLETED".equals(attempt.status())) {
            log.warn("User {} tried to review non-completed attempt {}", userId, request.attemptId());
            throw new IllegalStateException("Can only review completed attempts");
        }

        if (reviewRepository.existsByUserIdAndAttemptId(userId, request.attemptId())) {
            log.warn("User {} already reviewed attempt {}", userId, request.attemptId());
            throw new IllegalStateException("Review already exists for this attempt");
        }

        Review review = Review.create(
                userId,
                request.attemptId(),
                request.rating(),
                request.comment(),
                clock.now()
        );

        review = reviewRepository.save(review);
        log.info("Review created successfully: {}", review.getId());

        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                review.getAttemptId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewResponse> getReviewByAttempt(UUID userId, UUID attemptId) {
        log.debug("Getting review for attempt {} by user {}", attemptId, userId);

        return reviewRepository.findByUserIdAndAttemptId(userId, attemptId)
                .map(review -> new ReviewResponse(
                        review.getId(),
                        review.getUserId(),
                        review.getAttemptId(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt()
                ));
    }
}

