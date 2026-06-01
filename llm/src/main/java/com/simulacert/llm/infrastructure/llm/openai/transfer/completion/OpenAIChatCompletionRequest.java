package com.simulacert.llm.infrastructure.llm.openai.transfer.completion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAIChatCompletionRequest(
        String model,
        List<Message> messages,
        Double temperature,
        @JsonProperty("max_output_tokens") Integer maxOutputTokens) {

    public record Message(String role, String content) {
    }
}