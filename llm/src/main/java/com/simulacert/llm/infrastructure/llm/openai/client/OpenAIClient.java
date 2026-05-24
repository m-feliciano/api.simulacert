package com.simulacert.llm.infrastructure.llm.openai.client;

import com.simulacert.llm.feign.FeignCustomConfig;
import com.simulacert.llm.infrastructure.llm.openai.transfer.OpenAIRequest;
import com.simulacert.llm.infrastructure.llm.openai.transfer.OpenAIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        value = "openai",
        url = "${feign.client.config.openai.url:https://api.openai.com/v1}",
        configuration = FeignCustomConfig.class
)
public interface OpenAIClient {

    @PostMapping(value = "/chat/completions", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OpenAIResponse> createChatCompletion(@RequestBody OpenAIRequest request,
                                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);
}