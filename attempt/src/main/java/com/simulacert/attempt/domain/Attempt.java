package com.simulacert.attempt.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import com.simulacert.attempt.application.dto.AttemptVo;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "attempts")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Attempt {

    @Id
    private UUID id;

    @Column(nullable = false, name = "user_id")
    private UUID userId;

    @Column(nullable = false, name = "exam_id")
    private UUID examId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AttemptStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(nullable = false)
    private boolean paused;

    @Column(name = "paused_at")
    private Instant pausedAt;

    @Column(name = "paused_remaining_seconds")
    private Long pausedRemainingSeconds;

    @Column(name = "score")
    private Integer score;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "attempt_questions", joinColumns = @JoinColumn(name = "attempt_id"))
    @Column(name = "question_id")
    @Builder.Default
    private List<UUID> questionIds = new ArrayList<>();

    @Column(nullable = false)
    private long seed;

    @Version
    private long version;

    public static Attempt create(UUID userId, UUID examId, List<UUID> questionIds, Instant startedAt, long seed) {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(examId, "examId cannot be null");
        Objects.requireNonNull(questionIds, "questionIds cannot be null");

        if (questionIds.isEmpty()) throw new IllegalArgumentException("questionIds cannot be empty");

        return Attempt.builder()
                .id(UuidCreator.getTimeOrdered())
                .userId(userId)
                .examId(examId)
                .status(AttemptStatus.IN_PROGRESS)
                .startedAt(startedAt)
                .questionIds(new ArrayList<>(questionIds))
                .seed(seed)
                .paused(false)
                .build();
    }

    public void finish(int score, Instant finishedAt) {
        if (this.status != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot finish an attempt that is not in progress");
        }

        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }
        if (finishedAt == null) {
            throw new IllegalArgumentException("finishedAt cannot be null");
        }

        this.status = AttemptStatus.COMPLETED;
        this.score = score;
        this.finishedAt = finishedAt;
    }

    public AttemptVo toVo() {
        return AttemptVo.builder()
                .id(id)
                .userId(userId)
                .examId(examId)
                .status(String.valueOf(status))
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .score(score)
                .questionIds(new ArrayList<>(questionIds))
                .endsAt(endsAt)
                .paused(paused)
                .pausedAt(pausedAt)
                .pausedRemainingSeconds(pausedRemainingSeconds)
                .build();
    }

    public void cancel(Instant now) {
        if (this.status != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot cancel an attempt that is not in progress");
        }
        this.status = AttemptStatus.ABANDONED;
        this.finishedAt = now;
    }

    public void initTimer(long durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("durationSeconds must be > 0");
        }

        if (this.startedAt == null) {
            throw new IllegalStateException("startedAt must be set to initialize timer");
        }

        if (this.endsAt != null) {
            return;
        }

        this.endsAt = this.startedAt.plusSeconds(durationSeconds);
        this.paused = false;
        this.pausedAt = null;
        this.pausedRemainingSeconds = null;
    }

    public void pause(Instant now) {
        requireInProgress();
        if (paused) return;

        if (endsAt == null) {
            throw new IllegalStateException("Timer not initialized");
        }

        long remaining = Math.max(0, endsAt.getEpochSecond() - now.getEpochSecond());
        this.paused = true;
        this.pausedAt = now;
        this.pausedRemainingSeconds = remaining;
    }

    public void resume(Instant now) {
        requireInProgress();
        if (!paused) return;

        if (pausedRemainingSeconds == null) {
            throw new IllegalStateException("pausedRemainingSeconds missing");

        }
        this.endsAt = now.plusSeconds(pausedRemainingSeconds);
        this.paused = false;
        this.pausedAt = null;
    }

    public void heartbeat(Instant now) {
        requireInProgress();
        if (endsAt == null) {
            throw new IllegalStateException("Timer not initialized");
        }

        if (!paused && now.isAfter(endsAt)) {
            this.endsAt = now;
        }
    }

    public long remainingSeconds(Instant now) {
        if (paused) {
            return pausedRemainingSeconds == null ? 0 : Math.max(0, pausedRemainingSeconds);
        }
        if (endsAt == null) {
            return 0;
        }
        return Math.max(0, endsAt.getEpochSecond() - now.getEpochSecond());
    }

    private void requireInProgress() {
        if (this.status != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Attempt must be IN_PROGRESS");
        }
    }
}
