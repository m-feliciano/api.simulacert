package com.simulacert.llm.infrastructure.llm.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAIRequest(
        String model,
        List<Message> messages,
        Double temperature,
        @JsonProperty("max_tokens") Integer maxTokens
) {
    public record Message(
            String role,
            String content
    ) {
    }
}