package com.simulacert.llm.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExplanationCacheService Tests")
class ExplanationCacheServiceTest {

    @InjectMocks
    private ExplanationCacheService cacheService;

    private String cacheKey;
    private UUID questionId;
    private String language;

    @BeforeEach
    void setUp() {
        questionId = UUID.randomUUID();
        language = "pt";
        cacheKey = questionId + ":" + language + ":v1.0";
    }

    @Test
    @DisplayName("Should return null on cache miss (getExplanation)")
    void shouldReturnNullOnCacheMiss() {
        // When
        String result = cacheService.getExplanation(cacheKey, questionId, language);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return content when putting in cache")
    void shouldReturnContentWhenPuttingInCache() {
        // Given
        String content = "This is a test explanation about AWS services.";

        // When
        String result = cacheService.putExplanation(cacheKey, content);

        // Then
        assertThat(result).isEqualTo(content);
    }

    @Test
    @DisplayName("Should handle null content in putExplanation")
    void shouldHandleNullContentInPutExplanation() {
        // When
        String result = cacheService.putExplanation(cacheKey, null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle empty cache key")
    void shouldHandleEmptyCacheKey() {
        // When
        String result = cacheService.getExplanation("", questionId, language);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle long content in putExplanation")
    void shouldHandleLongContentInPutExplanation() {
        // Given
        String longContent = "A".repeat(5000);

        // When
        String result = cacheService.putExplanation(cacheKey, longContent);

        // Then
        assertThat(result).isEqualTo(longContent);
        assertThat(result).hasSize(5000);
    }
}

