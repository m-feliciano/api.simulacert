package com.simulacert.config.security.dev;

import com.simulacert.config.security.CorsConfigurationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@Profile({"dev", "default"})
public class CorsConfigDev {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("CORS Configuration for DEV environment");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));

        return CorsConfigurationHelper.build(configuration);
    }
}

