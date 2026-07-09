package com.simulacert.infrastructure.xray;

import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

@Slf4j
@Configuration
@Profile("prod")
public class XRayConfig {

    @Value("${spring.application.name:simulacert-prod}")
    private String tracingName;

    @Bean
    public FilterRegistrationBean<AWSXRayServletFilter> xrayFilter() {
        FilterRegistrationBean<AWSXRayServletFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AWSXRayServletFilter(tracingName));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
