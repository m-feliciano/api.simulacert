package com.simulacert.llm.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ExplanationCacheService {

    @Cacheable(value = "explanations", key = "#cacheKey")
    public String getExplanation(String cacheKey, UUID questionId, String language) {
        log.debug("Cache miss for key: {} (question: {}, language: {})", cacheKey, questionId, language);
        // Spring Cache will store the generated content when returned.
        return null;
    }

    @CachePut(value = "explanations", key = "#cacheKey")
    public String putExplanation(String cacheKey, String content) {
        log.debug("Caching explanation for key: {}", cacheKey);
        return content;
    }
}

