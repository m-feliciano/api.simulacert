package com.simulacert.translation.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "translations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_translations_entity_field_lang",
                        columnNames = {"entity_type", "entity_id", "content", "language"}
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Translation {

    @Id
    private UUID id;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private TranslationSource source;

    @Column(name = "reviewed", nullable = false)
    private boolean reviewed;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public static Translation createLLM(
            String entityType,
            UUID entityId,
            String field,
            String language,
            String value,
            Instant now
    ) {
        Objects.requireNonNull(entityType, "entityType cannot be null");
        Objects.requireNonNull(entityId, "entityId cannot be null");
        Objects.requireNonNull(field, "content cannot be null");
        Objects.requireNonNull(language, "language cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        Objects.requireNonNull(now, "now cannot be null");

        return Translation.builder()
                .id(UuidCreator.getTimeOrdered())
                .entityType(entityType)
                .entityId(entityId)
                .content(field)
                .language(language)
                .value(value)
                .source(TranslationSource.LLM)
                .reviewed(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void markReviewed(Instant now) {
        this.reviewed = true;
        this.updatedAt = now;
    }

    public void updateManualValue(String newValue, Instant now) {
        Objects.requireNonNull(newValue, "newValue cannot be null");
        Objects.requireNonNull(now, "now cannot be null");
        this.value = newValue;
        this.source = TranslationSource.MANUAL;
        this.updatedAt = now;
    }
}

