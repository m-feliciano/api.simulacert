package com.simulacert.service.impl;

import com.simulacert.service.TracingService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.xray.enabled", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockTracingService implements TracingService {

    public Object execute(ProceedingJoinPoint pjp) throws Throwable {
        log.info("Executing method: {}", ((MethodSignature) pjp.getSignature()).getMethod().getName());
        return pjp.proceed();
    }

    public Object executeAnnotation(ProceedingJoinPoint pjp) throws Throwable {
        log.info("Executing method: {}", ((MethodSignature) pjp.getSignature()).getMethod().getName());
        return pjp.proceed();
    }

    public void putAnnotation(String key, Object value) {
        log.info("Putting annotation: {}={}", key, value);
    }
}