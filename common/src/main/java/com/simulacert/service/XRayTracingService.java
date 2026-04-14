package com.simulacert.service;

import com.amazonaws.xray.AWSXRay;
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
        if (ann == null) {
            return pjp.proceed();
        }

        String name = resolveName(method, ann);

        Subsegment sub = AWSXRay.beginSubsegment(name);

        applyUserEntityTracing();

        try {
            Object result = pjp.proceed();

            if (ann.captureArgs()) {
                addSafeMetadata(sub, sig, pjp.getArgs());
            }

            return result;

        } catch (Throwable t) {
            try {
                sub.addException(t);
            } catch (Exception ignored) {
            }

            throw t;

        } finally {
            sub.close();
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

    private void addSafeMetadata(Subsegment sub, MethodSignature sig, Object[] args) {
        String[] names = sig.getParameterNames();
        if (names == null || args == null) return;

        for (int i = 0; i < Math.min(names.length, args.length); i++) {
            sub.putMetadata("args", names[i], sanitize(args[i]));
        }
    }

    private Object sanitize(Object value) {
        if (value == null) return null;

        if (value instanceof String s) {
            return s.length() > 64 ? s.substring(0, 64) + "..." : s;
        }

        if (value instanceof Number || value instanceof Boolean) {
            return value;
        }

        return value.getClass().getSimpleName();
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