package com.simulacert.infrastructure.xray;

import com.simulacert.service.XRayTracingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class XRayTracingAspect {

    private final XRayTracingService xray;

    @Around("@annotation(com.simulacert.infrastructure.xray.XRaySubsegment)")
    public Object aroundSubsegment(ProceedingJoinPoint pjp) throws Throwable {
        return xray.execute(pjp);
    }

    @Around("@annotation(com.simulacert.infrastructure.xray.XRayAnnotation)")
    public Object aroundAnnotation(ProceedingJoinPoint pjp) throws Throwable {
        return xray.executeAnnotation(pjp);
    }
}