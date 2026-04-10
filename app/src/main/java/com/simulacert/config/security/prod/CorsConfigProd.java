package com.simulacert.config.security.prod;

import com.simulacert.config.security.CorsConfigurationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;

@Slf4j
@Configuration
@Profile("prod")
public class CorsConfigProd {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("CORS Configuration for PROD environment");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.asList(
                        "https://app.simulacert.com",
                        "https://admin.simulacert.com"
                )
        );

        return CorsConfigurationHelper.build(configuration);
    }
}

