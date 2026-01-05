package com.simulacert.llm.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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

    @CacheEvict(value = "requests", allEntries = true)
    public void evictRequests() {
        log.info("Evicting all entries from requests cache");
    }

    @Cacheable(value = "requests", key = "#userId")
    public Integer getRequestCount(UUID userId, Integer count) {
        log.info("Cache miss for key: {} (count: {})", userId, count);
        // Spring Cache will store the generated count when returned.
        return null;
    }

    @CachePut(value = "requests", key = "#userId")
    public void putRequestCount(UUID userId, Integer count) {
        log.info("Caching request count for user: {} with count: {}", userId, count);
    }
}