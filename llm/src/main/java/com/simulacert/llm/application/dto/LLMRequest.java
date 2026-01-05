package com.simulacert.llm.application.dto;

public record LLMRequest(
        String systemPrompt,
        String userPrompt,
        Double temperature,
        Integer maxTokens
) {
}