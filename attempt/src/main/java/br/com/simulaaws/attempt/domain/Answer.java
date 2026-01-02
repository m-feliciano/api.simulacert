package br.com.simulaaws.attempt.domain;

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
@Table(name = "answers")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    @Id
    private UUID id;

    @Column(nullable = false, name = "attempt_id")
    private UUID attemptId;

    @Column(nullable = false, name = "question_id")
    private UUID questionId;

    @Column(nullable = false, length = 10, name = "selected_option")
    private String selectedOption;

    @Column(nullable = false, name = "answered_at")
    private Instant answeredAt;

    public static Answer create(UUID attemptId, UUID questionId, String selectedOption, Instant answeredAt) {
        Objects.requireNonNull(attemptId, "attemptId cannot be null");
        Objects.requireNonNull(questionId, "questionId cannot be null");
        Objects.requireNonNull(selectedOption, "selectedOption cannot be null");
        Objects.requireNonNull(answeredAt, "answeredAt cannot be null");

        if (selectedOption.isBlank()) {
            throw new IllegalArgumentException("selectedOption cannot be blank");
        }

        return Answer.builder()
                .id(UUID.randomUUID())
                .attemptId(attemptId)
                .questionId(questionId)
                .selectedOption(selectedOption.trim())
                .answeredAt(answeredAt)
                .build();
    }
}

