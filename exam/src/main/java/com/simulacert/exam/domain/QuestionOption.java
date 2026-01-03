package com.simulacert.exam.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "question_options")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOption {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 1, name = "option_key")
    private String optionKey;

    @Column(nullable = false, length = 500, name = "option_text")
    private String optionText;

    @Column(nullable = false, name = "is_correct")
    private Boolean isCorrect;

    public static QuestionOption create(Question question, String optionKey, String optionText, Boolean isCorrect) {
        Objects.requireNonNull(question, "question cannot be null");
        Objects.requireNonNull(optionKey, "optionKey cannot be null");
        Objects.requireNonNull(optionText, "optionText cannot be null");
        Objects.requireNonNull(isCorrect, "isCorrect cannot be null");

        return QuestionOption.builder()
                .id(UuidCreator.getTimeOrdered())
                .question(question)
                .optionKey(optionKey.trim().toUpperCase())
                .optionText(optionText.trim())
                .isCorrect(isCorrect)
                .build();
    }
}

