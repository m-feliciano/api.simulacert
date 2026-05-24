package com.simulacert.llm.infrastructure.llm.openai;

import com.simulacert.infrastructure.xray.XRaySubsegment;
import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import com.simulacert.llm.infrastructure.llm.openai.client.OpenAIClient;
import com.simulacert.llm.infrastructure.llm.openai.transfer.OpenAIRequest;
import com.simulacert.llm.infrastructure.llm.openai.transfer.OpenAIResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.llm.openai", name = "enabled", havingValue = "true")
public class OpenAILLMProvider implements ExplanationLLMPort {

    @Value("${app.llm.openai.model:gpt-4.1-mini}")
    private String model;

    @Value("${app.llm.openai.api-key}")
    private String apiKey;

    private final OpenAIClient openAIClient;

    @Autowired
    public OpenAILLMProvider(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @Override
    @XRaySubsegment("ext.openai.chat.completions")
    public LLMResult generate(LLMRequest request) {
        log.info("Calling OpenAI API with model: {}", request.model() != null ? request.model() : model);
        log.debug("Temperature: {}, Max Tokens: {}", request.temperature(), request.maxTokens());

        OpenAIRequest openAIRequest = buildOpenAIRequest(request);
        var responseEntity = openAIClient.createChatCompletion(openAIRequest, "Bearer " + apiKey);
        if (responseEntity == null || responseEntity.getBody() == null) {
            throw new IllegalStateException("OpenAI returned empty response");
        }

        OpenAIResponse openAIResponse = responseEntity.getBody();
        String content = openAIResponse.choices().getFirst().message().content();

        log.info("OpenAI API call successful. Tokens used: {}",
                openAIResponse.usage() != null ? openAIResponse.usage().totalTokens() : "unknown");

        return new LLMResult(content, openAIResponse.model(), "openai");
    }

    private OpenAIRequest buildOpenAIRequest(LLMRequest request) {
        List<OpenAIRequest.Message> messages = List.of(
                new OpenAIRequest.Message("system", request.systemPrompt()),
                new OpenAIRequest.Message("user", request.userPrompt())
        );

        return new OpenAIRequest(
                ObjectUtils.defaultIfNull(request.model(), model),
                messages,
                request.temperature(),
                request.maxTokens()
        );
    }
}

