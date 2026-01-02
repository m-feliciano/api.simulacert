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
@Table(name = "exams")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exam {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    public static Exam create(String title, String description) {
        Objects.requireNonNull(title, "title cannot be null");
        if (title.trim().isEmpty()) {
            throw new IllegalArgumentException("title cannot be empty");
        }

        return Exam.builder()
                .id(UUID.randomUUID())
                .title(title.trim())
                .description(description != null ? description.trim() : null)
                .build();
    }

    public void update(String title, String description) {
        Objects.requireNonNull(title, "title cannot be null");
        if (title.trim().isEmpty()) {
            throw new IllegalArgumentException("title cannot be empty");
        }

        this.title = title.trim();
        this.description = description != null ? description.trim() : null;
    }
}
