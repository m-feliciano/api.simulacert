package com.simulacert.llm.infrastructure.llm.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simulacert.infrastructure.xray.XRaySubsegment;
import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.dto.PromptRequest;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import com.simulacert.llm.infrastructure.llm.openai.client.OpenAIClient;
import com.simulacert.llm.infrastructure.llm.openai.transfer.completion.OpenAIChatCompletionRequest;
import com.simulacert.llm.infrastructure.llm.openai.transfer.completion.OpenAIChatCompletionResponse;
import com.simulacert.llm.infrastructure.llm.openai.transfer.prompt.OpenAIPromptRequest;
import com.simulacert.llm.infrastructure.llm.openai.transfer.prompt.OpenAIPromptResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.llm.openai", name = "enabled", havingValue = "true")
public class OpenAILLMProvider implements ExplanationLLMPort {

    @Value("${app.llm.openai.model}")
    private String model;

    @Value("${app.llm.openai.api-key}")
    private String apiKey;

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public OpenAILLMProvider(OpenAIClient openAIClient,
                             ObjectMapper objectMapper) {
        this.openAIClient = openAIClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @XRaySubsegment("ext.openai.chat.completions")
    public LLMResult generate(LLMRequest request, int maxOutputTokens) {
        log.info("Calling OpenAI API with model: {}", request.model() != null ? request.model() : model);
        log.debug("Temperature: {}, Max Tokens: {}", request.temperature(), maxOutputTokens);

        List<OpenAIChatCompletionRequest.Message> messages = List.of(
                new OpenAIChatCompletionRequest.Message("system", request.systemPrompt()),
                new OpenAIChatCompletionRequest.Message("user", request.userPrompt())
        );

        OpenAIChatCompletionRequest openAIRequest = new OpenAIChatCompletionRequest(
                ObjectUtils.getIfNull(request.model(), model),
                messages,
                request.temperature(),
                maxOutputTokens
        );

        var responseEntity = openAIClient.createChatCompletion(openAIRequest, "Bearer " + apiKey);
        if (responseEntity == null || responseEntity.getBody() == null) {
            throw new IllegalStateException("OpenAI returned empty response");
        }

        OpenAIChatCompletionResponse openAIResponse = responseEntity.getBody();
        String content = openAIResponse.choices().getFirst().message().content();

        log.info("OpenAI API call successful. Tokens used: {}", openAIResponse.usage() != null ? openAIResponse.usage().totalTokens() : "unknown");

        return new LLMResult(content, openAIResponse.model(), "openai");
    }


    @Override
    public LLMResult generate(PromptRequest request, int maxOutputTokens) {
        Objects.requireNonNull(request, "PromptRequest cannot be null");
        Objects.requireNonNull(request.variables(), "PromptRequest variables cannot be null");

        var openAIRequest = buildOpenAIPromptRequest(request, maxOutputTokens);
        var responseEntity = openAIClient.createPrompt(openAIRequest, "Bearer " + apiKey);

        OpenAIPromptResponse body = responseEntity.getBody();
        if (body == null) {
            throw new IllegalStateException("OpenAI returned empty response");
        }

        OpenAIPromptResponse.Usage usage = body.usage();
        if (usage == null) {
            log.warn("OpenAI response does not contain usage information");
        } else {
            log.info("OpenAI API call successful.\nInput Tokens: {}, Output Tokens: {}, Total Tokens: {}",
                    usage.inputTokens(), usage.outputTokens(), usage.totalTokens());
        }

        return new LLMResult(extractContent(body), body.model(), "openai");
    }

    @SuppressWarnings("unchecked")
    private OpenAIPromptRequest buildOpenAIPromptRequest(PromptRequest request, int maxOutputTokens) {
        Map<String, Object> variables = objectMapper.convertValue(request.variables(), Map.class);
        return OpenAIPromptRequest.builder()
                .store(true)
                .prompt(
                        new OpenAIPromptRequest.Prompt(
                                request.prompt().id(),
                                "2",
                                variables
                        )
                )
                .reasoning(new OpenAIPromptRequest.Reasoning("auto"))
                .maxOutputTokens(maxOutputTokens)
                .build();
    }

    private static String extractContent(OpenAIPromptResponse body) {
        return body.output().stream()
                .filter(output -> "message".equalsIgnoreCase(output.type()))
                .findFirst()
                .orElseThrow()
                .content()
                .stream()
                .filter(c -> "output_text".equalsIgnoreCase(c.type()))
                .findFirst()
                .orElseThrow()
                .text();
    }
}

