package com.simulacert.review.application.service;

import com.simulacert.attempt.application.dto.AttemptVo;
import com.simulacert.common.ClockPort;
import com.simulacert.review.application.dto.CreateReviewRequest;
import com.simulacert.review.application.dto.ReviewResponse;
import com.simulacert.review.application.port.out.AttemptQueryPort;
import com.simulacert.review.application.port.out.ReviewRepositoryPort;
import com.simulacert.review.domain.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Tests")
class ReviewServiceTest {

    @Mock
    private ReviewRepositoryPort reviewRepository;

    @Mock
    private AttemptQueryPort attemptQueryPort;

    @Mock
    private ClockPort clock;

    @InjectMocks
    private ReviewService reviewService;

    private UUID userId;
    private UUID attemptId;
    private Instant now;
    private AttemptVo completedAttempt;
    private CreateReviewRequest validRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        attemptId = UUID.randomUUID();
        now = Instant.parse("2026-01-05T10:00:00Z");

        completedAttempt = AttemptVo.builder()
                .id(attemptId)
                .userId(userId)
                .examId(UUID.randomUUID())
                .status("COMPLETED")
                .startedAt(now.minusSeconds(3600))
                .finishedAt(now.minusSeconds(60))
                .score(85)
                .build();

        validRequest = new CreateReviewRequest(attemptId, 5, "Great exam!");
    }

    @Test
    @DisplayName("Should create review successfully")
    void shouldCreateReviewSuccessfully() {
        when(attemptQueryPort.findById(attemptId)).thenReturn(Optional.of(completedAttempt));
        when(reviewRepository.existsByUserIdAndAttemptId(userId, attemptId)).thenReturn(false);
        when(clock.now()).thenReturn(now);

        Review savedReview = Review.create(userId, attemptId, 5, "Great exam!", now);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponse response = reviewService.createReview(userId, validRequest);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.attemptId()).isEqualTo(attemptId);
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.comment()).isEqualTo("Great exam!");

        verify(attemptQueryPort).findById(attemptId);
        verify(reviewRepository).existsByUserIdAndAttemptId(userId, attemptId);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when attempt not found")
    void shouldThrowExceptionWhenAttemptNotFound() {
        when(attemptQueryPort.findById(attemptId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(userId, validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Attempt not found: " + attemptId);

        verify(attemptQueryPort).findById(attemptId);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when user does not own the attempt")
    void shouldThrowExceptionWhenUserDoesNotOwnAttempt() {
        UUID differentUserId = UUID.randomUUID();
        when(attemptQueryPort.findById(attemptId)).thenReturn(Optional.of(completedAttempt));

        assertThatThrownBy(() -> reviewService.createReview(differentUserId, validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You can only review your own attempts");

        verify(attemptQueryPort).findById(attemptId);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when attempt is not completed")
    void shouldThrowExceptionWhenAttemptNotCompleted() {
        AttemptVo inProgressAttempt = AttemptVo.builder()
                .id(attemptId)
                .userId(userId)
                .examId(UUID.randomUUID())
                .status("IN_PROGRESS")
                .startedAt(now.minusSeconds(600))
                .build();

        when(attemptQueryPort.findById(attemptId)).thenReturn(Optional.of(inProgressAttempt));

        assertThatThrownBy(() -> reviewService.createReview(userId, validRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Can only review completed attempts");

        verify(attemptQueryPort).findById(attemptId);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when review already exists")
    void shouldThrowExceptionWhenReviewAlreadyExists() {
        when(attemptQueryPort.findById(attemptId)).thenReturn(Optional.of(completedAttempt));
        when(reviewRepository.existsByUserIdAndAttemptId(userId, attemptId)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(userId, validRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Review already exists for this attempt");

        verify(attemptQueryPort).findById(attemptId);
        verify(reviewRepository).existsByUserIdAndAttemptId(userId, attemptId);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should create review without comment")
    void shouldCreateReviewWithoutComment() {
        CreateReviewRequest requestWithoutComment = new CreateReviewRequest(attemptId, 4, null);

        when(attemptQueryPort.findById(attemptId)).thenReturn(Optional.of(completedAttempt));
        when(reviewRepository.existsByUserIdAndAttemptId(userId, attemptId)).thenReturn(false);
        when(clock.now()).thenReturn(now);

        Review savedReview = Review.create(userId, attemptId, 4, null, now);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponse response = reviewService.createReview(userId, requestWithoutComment);

        assertThat(response).isNotNull();
        assertThat(response.rating()).isEqualTo(4);
        assertThat(response.comment()).isNull();

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("Should create review with minimum rating")
    void shouldCreateReviewWithMinimumRating() {
        CreateReviewRequest minRatingRequest = new CreateReviewRequest(attemptId, 1, "Too difficult");

        when(attemptQueryPort.findById(attemptId)).thenReturn(Optional.of(completedAttempt));
        when(reviewRepository.existsByUserIdAndAttemptId(userId, attemptId)).thenReturn(false);
        when(clock.now()).thenReturn(now);

        Review savedReview = Review.create(userId, attemptId, 1, "Too difficult", now);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponse response = reviewService.createReview(userId, minRatingRequest);

        assertThat(response).isNotNull();
        assertThat(response.rating()).isEqualTo(1);

        verify(reviewRepository).save(any(Review.class));
    }
}

