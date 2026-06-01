package com.simulacert.llm.infrastructure.llm.openai.transfer.prompt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAIPromptRequest(
        String model,
        Prompt prompt,
        Reasoning reasoning,
        Boolean store,
        List<String> include,
        @JsonProperty("max_output_tokens")
        Integer maxOutputTokens
) {

    public record Prompt(
            String id,
            String version,
            Map<String, Object> variables
    ) {
    }

    public record Reasoning(
            String summary
    ) {
    }
}