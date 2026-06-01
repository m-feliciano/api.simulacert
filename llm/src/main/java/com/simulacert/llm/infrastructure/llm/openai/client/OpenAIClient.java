package com.simulacert.llm.infrastructure.llm.openai.client;

import com.simulacert.llm.feign.FeignCustomConfig;
import com.simulacert.llm.infrastructure.llm.openai.transfer.completion.OpenAIChatCompletionRequest;
import com.simulacert.llm.infrastructure.llm.openai.transfer.completion.OpenAIChatCompletionResponse;
import com.simulacert.llm.infrastructure.llm.openai.transfer.prompt.OpenAIPromptRequest;
import com.simulacert.llm.infrastructure.llm.openai.transfer.prompt.OpenAIPromptResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        value = "openai",
        url = "${feign.client.config.openai.url:https://api.openai.com}",
        configuration = FeignCustomConfig.class
)
public interface OpenAIClient {

    @PostMapping(value = "/v1/chat/completions", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OpenAIChatCompletionResponse> createChatCompletion(@RequestBody OpenAIChatCompletionRequest request,
                                                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

    @PostMapping(value = "/v1/responses", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OpenAIPromptResponse> createPrompt(@RequestBody OpenAIPromptRequest request,
                                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);
}