package br.com.simulaaws.app.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Slf4j
@Configuration
public class LoggingConfig {

    private final Environment env;

    @Value("${spring.application.name}")
    private String appName;

    public LoggingConfig(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void logStartupInfo() {
        log.info("========================================");
        log.info("Application: {}", appName);
        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        log.info("Default profiles: {}", Arrays.toString(env.getDefaultProfiles()));
        log.info("========================================");

        log.debug("Logging configuration initialized successfully");

        if (Arrays.asList(env.getActiveProfiles()).contains("dev")) {
            log.warn("Running in DEVELOPMENT mode - Enhanced logging enabled");
        }

        if (Arrays.asList(env.getActiveProfiles()).contains("prod")) {
            log.info("Running in PRODUCTION mode - Optimized logging enabled");
        }
    }
}

