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
@Table(name = "question_options")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOption {

    @Id
    private UUID id;

    @Column(nullable = false, name = "question_id")
    private UUID questionId;

    @Column(nullable = false, length = 1, name = "option_key")
    private String optionKey;

    @Column(nullable = false, length = 500, name = "option_text")
    private String optionText;

    @Column(nullable = false, name = "is_correct")
    private Boolean isCorrect;

    public static QuestionOption create(UUID questionId, String optionKey, String optionText, Boolean isCorrect) {
        Objects.requireNonNull(questionId, "questionId cannot be null");
        Objects.requireNonNull(optionKey, "optionKey cannot be null");
        Objects.requireNonNull(optionText, "optionText cannot be null");
        Objects.requireNonNull(isCorrect, "isCorrect cannot be null");

        return QuestionOption.builder()
                .id(UUID.randomUUID())
                .questionId(questionId)
                .optionKey(optionKey.trim().toUpperCase())
                .optionText(optionText.trim())
                .isCorrect(isCorrect)
                .build();
    }
}

