package br.com.simulaaws.exam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    private UUID id;

    @Column(nullable = false, name = "exam_id")
    private UUID examId;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false, length = 100)
    private String domain;

    @Column(nullable = false, length = 50)
    private String difficulty;

    public static Question create(UUID examId, String text, String domain, String difficulty) {
        Objects.requireNonNull(examId, "examId cannot be null");
        Objects.requireNonNull(text, "text cannot be null");
        Objects.requireNonNull(domain, "domain cannot be null");
        Objects.requireNonNull(difficulty, "difficulty cannot be null");

        return Question.builder()
                .id(UUID.randomUUID())
                .examId(examId)
                .text(text.trim())
                .domain(domain.trim())
                .difficulty(difficulty.trim())
                .build();
    }
}

