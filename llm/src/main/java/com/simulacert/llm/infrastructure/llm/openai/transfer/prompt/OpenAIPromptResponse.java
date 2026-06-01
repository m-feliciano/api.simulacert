package com.simulacert.llm.infrastructure.llm.openai.transfer.prompt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAIPromptResponse(
        String id,
        String object,
        @JsonProperty("created_at") Long createdAt,
        String status,
        String model,
        List<Output> output,
        Usage usage
) {
    public record Output(
            String id,
            String type,
            String role,
            List<Content> content
    ) {
    }

    public record Content(
            String type,
            String text
    ) {
    }

    public record Usage(
            @JsonProperty("input_tokens") Integer inputTokens,
            @JsonProperty("output_tokens") Integer outputTokens,
            @JsonProperty("total_tokens") Integer totalTokens
    ) {
    }
}