package com.simulacert.config;

import com.simulacert.common.ClockPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApplicationConfig {

    @Bean
    public ClockPort clockPort() {
        return Clock.systemUTC()::instant;
    }
}
