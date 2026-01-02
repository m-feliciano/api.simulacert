package br.com.simulaaws.config;

import br.com.simulaaws.common.ClockPort;
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
