package com.simulacert.llm.infrastructure.llm.openai;

import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.dto.PromptRequest;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.llm.openai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockLLMProvider implements ExplanationLLMPort {

    public static final String MOCK_LLM_RESPONSE = """
            **************************************
            Really impressive output response here!!!
            This feature is still in its early stages, but we are working hard to improve it.
            Stay tuned for more updates.
            **************************************
            """;

    @Override
    public LLMResult generate(LLMRequest request, int maxOutputTokens) {
        log.info("Mock LLM Provider - Generating explanation");
        log.debug("System Prompt: {}", request.systemPrompt());
        log.debug("User Prompt: {}", request.userPrompt());
        log.debug("Temperature: {}, Max Tokens: {}", request.temperature(), maxOutputTokens);

        return new LLMResult(
                MOCK_LLM_RESPONSE,
                "gpt-4-mock",
                "mock-openai"
        );
    }

    @Override
    public LLMResult generate(PromptRequest request, int maxOutputTokens) {
        log.info("Mock LLM Provider - Generating explanation");
        log.info("Prompt ID: {}, Variables: {}", request.prompt().id(), request.variables());
        log.debug("Max Tokens: {}", maxOutputTokens);

        return new LLMResult(
                MOCK_LLM_RESPONSE,
                request.prompt().id() + "-mock",
                "mock-openai"
        );
    }
}

