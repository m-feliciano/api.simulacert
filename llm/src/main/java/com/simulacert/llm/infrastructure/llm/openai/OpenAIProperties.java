package com.simulacert.llm.infrastructure.llm.openai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.llm.openai")
public class OpenAIProperties {

    private String apiKey;
    private String model = "gpt-4o-mini";
    private String baseUrl = "https://api.openai.com/v1";
    private Integer timeoutSeconds = 30;
    private Boolean enabled = false;
}

