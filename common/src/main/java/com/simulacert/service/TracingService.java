package com.simulacert.service;

import org.aspectj.lang.ProceedingJoinPoint;

public interface TracingService {

    Object execute(ProceedingJoinPoint pjp) throws Throwable;

    Object executeAnnotation(ProceedingJoinPoint pjp) throws Throwable;

    void putAnnotation(String key, Object value);
}