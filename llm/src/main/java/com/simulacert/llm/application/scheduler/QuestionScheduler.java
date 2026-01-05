package com.simulacert.llm.application.scheduler;

import com.simulacert.llm.application.service.ExplanationCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionScheduler {

    private final ExplanationCacheService cacheService;

    @Scheduled(fixedRate = 60000 * 60) // Every hour
    public void clearCache() {
        cacheService.evictRequests();
    }
}
