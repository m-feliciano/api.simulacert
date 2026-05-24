package com.simulacert.llm.feign;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignCustomConfig {

    @Bean
    public ErrorDecoder feignDecoder() {
        return new FeignCustomDecoder();
    }
}
