package com.simulacert.llm.infrastructure.llm.openai;

import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.llm.openai", name = "enabled", havingValue = "true")
public class OpenAILLMProvider implements ExplanationLLMPort {

    @Autowired
    private OpenAIProperties properties;
    private RestClient restClient;

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("OpenAI LLM Provider initialized with model: {}", properties.getModel());
    }

    @Override
    public LLMResult generate(LLMRequest request) {
        log.info("Calling OpenAI API with model: {}", properties.getModel());
        log.debug("Temperature: {}, Max Tokens: {}", request.temperature(), request.maxTokens());

        try {
            OpenAIRequest openAIRequest = buildOpenAIRequest(request);

            OpenAIResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(openAIRequest)
                    .retrieve()
                    .body(OpenAIResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new IllegalStateException("OpenAI returned empty response");
            }

            String content = response.choices().getFirst().message().content();

            log.info("OpenAI API call successful. Tokens used: {}",
                    response.usage() != null ? response.usage().totalTokens() : "unknown");

            return new LLMResult(
                    content,
                    response.model(),
                    "openai"
            );

        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to generate explanation using OpenAI: " + e.getMessage(), e);
        }
    }

    private OpenAIRequest buildOpenAIRequest(LLMRequest request) {
        List<OpenAIRequest.Message> messages = List.of(
                new OpenAIRequest.Message("system", request.systemPrompt()),
                new OpenAIRequest.Message("user", request.userPrompt())
        );

        return new OpenAIRequest(
                properties.getModel(),
                messages,
                request.temperature(),
                request.maxTokens()
        );
    }
}

