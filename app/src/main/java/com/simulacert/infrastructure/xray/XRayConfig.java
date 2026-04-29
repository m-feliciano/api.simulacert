package com.simulacert.infrastructure.xray;

import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class XRayConfig {

    @Bean
    public Filter TracingFilter() {
        log.info("Initializing AWS X-Ray Servlet Filter for tracing");

        return new AWSXRayServletFilter("simulacert-prod");
    }
}
