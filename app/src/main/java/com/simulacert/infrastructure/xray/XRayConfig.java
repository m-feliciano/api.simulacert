package com.simulacert.infrastructure.xray;

import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("prod")
public class XRayConfig {

    @Value("${spring.application.name:simulacert-prod}")
    private String tracingName;

    @Bean
    public Filter TracingFilter() {
        log.info("Initializing AWS X-Ray Servlet Filter for tracing");

        return new AWSXRayServletFilter(tracingName);
    }
}
