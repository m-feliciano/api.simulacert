package com.simulacert.exam.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_feedback")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedback {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "user_rating", nullable = false)
    private Integer userRating;

    @Column(name = "feedback", length = 1000)
    private String feedback;

    @Column(name = "rated_at", nullable = false)
    private Instant ratedAt;

    @Column(name = "question_explanation_run_id")
    private UUID questionExplanationRun;

    public static UserFeedback create(Integer rating,
                                      String content,
                                      Instant ratedAt,
                                      UUID userId,
                                      UUID questionExplanationRunId) {
        Objects.requireNonNull(rating, "rating cannot be null");
        Objects.requireNonNull(ratedAt, "ratedAt cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }

        return UserFeedback.builder()
                .id(UuidCreator.getTimeOrdered())
                .userId(userId)
                .userRating(rating)
                .feedback(content.trim())
                .ratedAt(ratedAt)
                .questionExplanationRun(questionExplanationRunId)
                .build();
    }
}

