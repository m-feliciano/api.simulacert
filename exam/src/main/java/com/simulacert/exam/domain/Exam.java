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

    @Column(nullable = false, unique = true)
    private String slug;

    public static Exam create(String title, String description, String slug) {
        Objects.requireNonNull(title, "title cannot be null");
        if (title.trim().isEmpty()) {
            throw new IllegalArgumentException("title cannot be empty");
        }

        Objects.requireNonNull(description, "description cannot be null");
        Objects.requireNonNull(slug, "slug cannot be null");

        return Exam.builder()
                .id(UuidCreator.getTimeOrdered())
                .title(title.trim())
                .description(description.trim())
                .slug(slug.trim())
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
