package com.simulacert.exam.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "question_explanation_runs")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionExplanationRun {

    @Id
    private UUID id;

    @Column(nullable = false, name = "question_id")
    private UUID questionId;

    @Column(nullable = false, length = 50, name = "model_provider")
    private String modelProvider;

    @Column(nullable = false, length = 100, name = "model_name")
    private String modelName;

    @Column(nullable = false, length = 20, name = "prompt_version")
    private String promptVersion;

    @Column(name = "user_id")
    private UUID userId; // who requested the explanation, can be null

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "questionExplanationRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFeedback> userFeedbacks;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "expires_at")
    private Instant expiresAt;

    public static QuestionExplanationRun create(
            UUID questionId,
            String modelProvider,
            String modelName,
            String promptVersion,
            Double temperature,
            String language,
            String content,
            Instant createdAt,
            Instant expiresAt,
            UUID userId
    ) {
        Objects.requireNonNull(questionId, "questionId cannot be null");
        Objects.requireNonNull(modelProvider, "modelProvider cannot be null");
        Objects.requireNonNull(modelName, "modelName cannot be null");
        Objects.requireNonNull(promptVersion, "promptVersion cannot be null");
        Objects.requireNonNull(temperature, "temperature cannot be null");
        Objects.requireNonNull(language, "language cannot be null");
        Objects.requireNonNull(content, "content cannot be null");
        Objects.requireNonNull(createdAt, "createdAt cannot be null");
        Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");

        if (temperature < 0.0 || temperature > 2.0) {
            throw new IllegalArgumentException("temperature must be between 0.0 and 2.0");
        }

        return QuestionExplanationRun.builder()
                .id(UuidCreator.getTimeOrdered())
                .questionId(questionId)
                .modelProvider(modelProvider)
                .modelName(modelName)
                .promptVersion(promptVersion)
                .temperature(temperature)
                .language(language)
                .content(content)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .userId(userId)
                .build();
    }

    public void addFeedback(Integer rating, String content, Instant ratedAt, UUID userId) {
        if (this.userFeedbacks == null) {
            this.userFeedbacks = new java.util.ArrayList<>();
        }

        this.userFeedbacks.add(UserFeedback.create(rating, content, ratedAt, userId, this.id));
    }
}

