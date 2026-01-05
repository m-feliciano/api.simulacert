package com.simulacert.llm.application.dto;

public record LLMResult(
        String content,
        String modelName,
        String provider
) {
}