package com.simulacert.llm.application.port.out;

import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.dto.PromptRequest;

public interface ExplanationLLMPort {
    /**
     * Generate an explanation using the LLM.
     *
     * @param request The LLM request containing the system prompt, user prompt, and generation parameters.
     * @return The LLM result containing the generated explanation.
     */
    LLMResult generate(LLMRequest request, int maxOutputTokens);

    /**
     * Generate an explanation using the LLM.
     *
     * @param request         The prompt request containing the prompt ID, version, and variables to populate the prompt template.
     * @param maxOutputTokens The maximum number of tokens to generate in the output.
     * @return The LLM result containing the generated explanation.
     */
    LLMResult generate(PromptRequest request, int maxOutputTokens);
}