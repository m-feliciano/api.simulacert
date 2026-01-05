package com.simulacert.adapter.llm;

import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.llm.openai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockLLMProvider implements ExplanationLLMPort {

    @Override
    public LLMResult generate(LLMRequest request) {
        log.info("Mock LLM Provider - Generating explanation");
        log.debug("System Prompt: {}", request.systemPrompt());
        log.debug("User Prompt: {}", request.userPrompt());
        log.debug("Temperature: {}, Max Tokens: {}", request.temperature(), request.maxTokens());

        String mockContent = generateMockExplanation(request.userPrompt());

        return new LLMResult(
                mockContent,
                "gpt-4-mock",
                "mock-openai"
        );
    }

    private String generateMockExplanation(String userPrompt) {
        return """
                 **************************************
                 Really impressive output response here!!!
                 This feature is still in its early stages, but we are working hard to improve it.
                 Stay tuned for more updates.
                 **************************************
                 """;
    }
}

