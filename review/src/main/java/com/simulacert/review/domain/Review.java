package com.simulacert.review.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_attempt",
        columnNames = {"user_id", "attempt_id"}
    )
)
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    private UUID id;

    @Column(nullable = false, name = "user_id")
    private UUID userId;

    @Column(nullable = false, name = "attempt_id")
    private UUID attemptId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static Review create(UUID userId, UUID attemptId, Integer rating, String comment, Instant createdAt) {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(attemptId, "attemptId cannot be null");
        Objects.requireNonNull(rating, "rating cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }

        if (comment != null && comment.length() > 1000) {
            throw new IllegalArgumentException("comment must not exceed 1000 characters");
        }

        return Review.builder()
                .id(UuidCreator.getTimeOrdered())
                .userId(userId)
                .attemptId(attemptId)
                .rating(rating)
                .comment(comment != null ? comment.trim() : null)
                .createdAt(createdAt)
                .build();
    }
}

