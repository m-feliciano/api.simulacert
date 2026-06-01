package com.simulacert.llm.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PromptRequest(
        Prompt prompt,
        Variables variables
) {
    @Builder
    public record Prompt(String id, String version) {
    }

    @Builder
    public record Variables(
            String language,
            String provider,
            String question,
            String options,
            @JsonProperty("correct_answers")
            String correctAnswers,
            String certification
    ) {
    }
}