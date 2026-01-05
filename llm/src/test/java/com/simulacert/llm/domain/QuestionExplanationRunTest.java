package com.simulacert.llm.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("QuestionExplanationRun Domain Tests")
class QuestionExplanationRunTest {

    @Test
    @DisplayName("Should create QuestionExplanationRun successfully")
    void shouldCreateQuestionExplanationRunSuccessfully() {
        // Given
        UUID questionId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        String provider = "openai";
        String model = "gpt-4";
        String version = "v1.0";
        Double temperature = 0.25;
        String language = "pt";
        String content = "Test explanation content";
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(172800); // 48h

        // When
        QuestionExplanationRun run = QuestionExplanationRun.create(
                questionId, attemptId, provider, model, version,
                temperature, language, content, now, expiresAt
        );

        // Then
        assertThat(run).isNotNull();
        assertThat(run.getId()).isNotNull();
        assertThat(run.getQuestionId()).isEqualTo(questionId);
        assertThat(run.getExamAttemptId()).isEqualTo(attemptId);
        assertThat(run.getModelProvider()).isEqualTo(provider);
        assertThat(run.getModelName()).isEqualTo(model);
        assertThat(run.getPromptVersion()).isEqualTo(version);
        assertThat(run.getTemperature()).isEqualTo(temperature);
        assertThat(run.getLanguage()).isEqualTo(language);
        assertThat(run.getContent()).isEqualTo(content);
        assertThat(run.getCreatedAt()).isEqualTo(now);
        assertThat(run.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(run.getUserRating()).isNull();
        assertThat(run.getUserFeedback()).isNull();
        assertThat(run.getRatedAt()).isNull();
    }

    @Test
    @DisplayName("Should create QuestionExplanationRun with null attemptId")
    void shouldCreateWithNullAttemptId() {
        // Given
        UUID questionId = UUID.randomUUID();
        Instant now = Instant.now();

        // When
        QuestionExplanationRun run = QuestionExplanationRun.create(
                questionId, null, "openai", "gpt-4", "v1.0",
                0.25, "en", "content", now, now.plusSeconds(3600)
        );

        // Then
        assertThat(run.getExamAttemptId()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when questionId is null")
    void shouldThrowExceptionWhenQuestionIdIsNull() {
        assertThatThrownBy(() -> QuestionExplanationRun.create(
                null, UUID.randomUUID(), "openai", "gpt-4", "v1.0",
                0.25, "en", "content", Instant.now(), Instant.now()
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("questionId cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when modelProvider is null")
    void shouldThrowExceptionWhenModelProviderIsNull() {
        assertThatThrownBy(() -> QuestionExplanationRun.create(
                UUID.randomUUID(), UUID.randomUUID(), null, "gpt-4", "v1.0",
                0.25, "en", "content", Instant.now(), Instant.now()
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("modelProvider cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when temperature is below 0")
    void shouldThrowExceptionWhenTemperatureBelowZero() {
        assertThatThrownBy(() -> QuestionExplanationRun.create(
                UUID.randomUUID(), UUID.randomUUID(), "openai", "gpt-4", "v1.0",
                -0.1, "en", "content", Instant.now(), Instant.now()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("temperature must be between 0.0 and 2.0");
    }

    @Test
    @DisplayName("Should throw exception when temperature is above 2.0")
    void shouldThrowExceptionWhenTemperatureAboveTwo() {
        assertThatThrownBy(() -> QuestionExplanationRun.create(
                UUID.randomUUID(), UUID.randomUUID(), "openai", "gpt-4", "v1.0",
                2.1, "en", "content", Instant.now(), Instant.now()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("temperature must be between 0.0 and 2.0");
    }

    @Test
    @DisplayName("Should add feedback successfully")
    void shouldAddFeedbackSuccessfully() {
        // Given
        QuestionExplanationRun run = QuestionExplanationRun.create(
                UUID.randomUUID(), UUID.randomUUID(), "openai", "gpt-4", "v1.0",
                0.25, "en", "content", Instant.now(), Instant.now().plusSeconds(3600)
        );
        Integer rating = 5;
        String feedback = "Excellent explanation!";
        Instant ratedAt = Instant.now();

        // When
        run.addFeedback(rating, feedback, ratedAt);

        // Then
        assertThat(run.getUserRating()).isEqualTo(rating);
        assertThat(run.getUserFeedback()).isEqualTo(feedback);
        assertThat(run.getRatedAt()).isEqualTo(ratedAt);
    }

    @Test
    @DisplayName("Should trim feedback when adding")
    void shouldTrimFeedbackWhenAdding() {
        // Given
        QuestionExplanationRun run = QuestionExplanationRun.create(
                UUID.randomUUID(), UUID.randomUUID(), "openai", "gpt-4", "v1.0",
                0.25, "en", "content", Instant.now(), Instant.now().plusSeconds(3600)
        );

        // When
        run.addFeedback(4, "  Feedback with spaces  ", Instant.now());

        // Then
        assertThat(run.getUserFeedback()).isEqualTo("Feedback with spaces");
    }

    @Test
    @DisplayName("Should accept null feedback")
    void shouldAcceptNullFeedback() {
        // Given
        QuestionExplanationRun run = QuestionExplanationRun.create(
                UUID.randomUUID(), UUID.randomUUID(), "openai", "gpt-4", "v1.0",
                0.25, "en", "content", Instant.now(), Instant.now().plusSeconds(3600)
        );

        // When
        run.addFeedback(3, null, Instant.now());

        // Then
        assertThat(run.getUserRating()).isEqualTo(3);
        assertThat(run.getUserFeedback()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when rating is null")
    void shouldThrowExceptionWhenRatingIsNull() {
        // Given
        QuestionExplanationRun run = QuestionExplanationRun.create(
                UUID.randomUUID(), UUID.randomUUID(), "openai", "gpt-4", "v1.0",
                0.25, "en", "content", Instant.now(), Instant.now().plusSeconds(3600)
        );

        // When/Then
        assertThatThrownBy(() -> run.addFeedback(null, "feedback", Instant.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("rating cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when rating is below 1")
    void shouldThrowExceptionWhenRatingBelowOne() {
        // Given
        QuestionExplanationRun run = QuestionExplanationRun.create(
                UUID.randomUUID(), UUID.randomUUID(), "openai", "gpt-4", "v1.0",
                0.25, "en", "content", Instant.now(), Instant.now().plusSeconds(3600)
        );

        // When/Then
        assertThatThrownBy(() -> run.addFeedback(0, "feedback", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rating must be between 1 and 5");
    }

    @Test
    @DisplayName("Should throw exception when rating is above 5")
    void shouldThrowExceptionWhenRatingAboveFive() {
        // Given
        QuestionExplanationRun run = QuestionExplanationRun.create(
                UUID.randomUUID(), UUID.randomUUID(), "openai", "gpt-4", "v1.0",
                0.25, "en", "content", Instant.now(), Instant.now().plusSeconds(3600)
        );

        // When/Then
        assertThatThrownBy(() -> run.addFeedback(6, "feedback", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rating must be between 1 and 5");
    }

    @Test
    @DisplayName("Should throw exception when ratedAt is null")
    void shouldThrowExceptionWhenRatedAtIsNull() {
        // Given
        QuestionExplanationRun run = QuestionExplanationRun.create(
                UUID.randomUUID(), UUID.randomUUID(), "openai", "gpt-4", "v1.0",
                0.25, "en", "content", Instant.now(), Instant.now().plusSeconds(3600)
        );

        // When/Then
        assertThatThrownBy(() -> run.addFeedback(5, "feedback", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ratedAt cannot be null");
    }

    @Test
    @DisplayName("Should update feedback when called multiple times")
    void shouldUpdateFeedbackWhenCalledMultipleTimes() {
        // Given
        QuestionExplanationRun run = QuestionExplanationRun.create(
                UUID.randomUUID(), UUID.randomUUID(), "openai", "gpt-4", "v1.0",
                0.25, "en", "content", Instant.now(), Instant.now().plusSeconds(3600)
        );

        // When
        run.addFeedback(3, "First feedback", Instant.now());
        run.addFeedback(5, "Updated feedback", Instant.now());

        // Then
        assertThat(run.getUserRating()).isEqualTo(5);
        assertThat(run.getUserFeedback()).isEqualTo("Updated feedback");
    }
}

