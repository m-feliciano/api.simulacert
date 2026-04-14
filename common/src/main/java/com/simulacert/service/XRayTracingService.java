package com.simulacert.service;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import com.simulacert.infrastructure.xray.XRayAnnotation;
import com.simulacert.infrastructure.xray.XRaySubsegment;
import com.simulacert.util.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class XRayTracingService {

    private final Map<Method, String> methodNameCache = new ConcurrentHashMap<>();

    public Object execute(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();

        XRaySubsegment ann = AnnotationUtils.findAnnotation(method, XRaySubsegment.class);
        Entity entity = AWSXRay.getTraceEntity();
        Segment currentSegment = AWSXRay.getCurrentSegment();

        if (ann == null || (entity == null && currentSegment == null)) {
            return pjp.proceed();
        }

        try (Subsegment sub = AWSXRay.beginSubsegment(resolveName(method, ann))) {
            applyUserEntityTracing();
            try {
                return pjp.proceed();
            } catch (Throwable t) {
                sub.addException(t);
                sub.setFault(true);
                throw t;
            }
        }
    }

    public Object executeAnnotation(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();

        XRayAnnotation ann = AnnotationUtils.findAnnotation(method, XRayAnnotation.class);
        if (ann == null) {
            return pjp.proceed();
        }

        applyUserEntityTracing();

        Object result = pjp.proceed();

        Object value = extractParam(sig, pjp.getArgs(), ann.param());
        if (value != null && AWSXRay.getTraceEntity() != null) {
            AWSXRay.getTraceEntity().putAnnotation(ann.key(), String.valueOf(value));
        }

        return result;
    }

    public void putAnnotation(String key, UUID value) {
        if (key == null || key.isBlank() || value == null) return;
        putAnnotationInternal(key, value.toString());
    }

    public void putAnnotation(String key, Number value) {
        if (key == null || key.isBlank() || value == null) return;
        putAnnotationInternal(key, value.toString());
    }

    public void putAnnotation(String key, String value) {
        if (key == null || key.isBlank() || value == null || value.isBlank()) return;
        putAnnotationInternal(key, value);
    }

    private void putAnnotationInternal(String key, String stringValue) {
        String safeValue = stringValue;
        if (safeValue.length() > 128) {
            safeValue = safeValue.substring(0, 128);
        }

        try {
            if (AWSXRay.getCurrentSegment() != null) {
                AWSXRay.getCurrentSegment().putAnnotation(key, safeValue);

            } else if (AWSXRay.getTraceEntity() != null) {
                AWSXRay.getTraceEntity().putAnnotation(key, safeValue);
            }
        } catch (Exception ex) {
            log.warn("Failed to put annotation {}: {}", key, safeValue, ex);
        }
    }

    private void applyUserEntityTracing() {
        UUID uuid = UserContextHolder.getUser();
        if (uuid == null) return;

        String userId = uuid.toString();

        if (AWSXRay.getCurrentSegment() != null) {
            AWSXRay.getCurrentSegment().putAnnotation("userId", userId);
        }
    }

    private String resolveName(Method method, XRaySubsegment ann) {
        return methodNameCache.computeIfAbsent(method, m -> {
            if (ann.value() != null && !ann.value().isBlank()) {
                return ann.value();
            }
            return m.getDeclaringClass().getSimpleName() + "." + m.getName();
        });
    }

    private Object extractParam(MethodSignature sig, Object[] args, String paramName) {
        if (paramName == null || paramName.isBlank()) return null;

        String[] names = sig.getParameterNames();
        if (names == null || args == null) return null;

        for (int i = 0; i < names.length; i++) {
            if (paramName.equals(names[i])) {
                return args[i];
            }
        }

        return null;
    }
}