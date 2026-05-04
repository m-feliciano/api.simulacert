package com.simulacert.llm.application.dto;

public record LLMRequest(
        String systemPrompt,
        String userPrompt,
        Double temperature,
        Integer maxTokens,
        String model
) {

    public LLMRequest(String systemPrompt, String userPrompt, Double temperature, Integer maxTokens) {
        this(systemPrompt, userPrompt, temperature, maxTokens, null);
    }
}