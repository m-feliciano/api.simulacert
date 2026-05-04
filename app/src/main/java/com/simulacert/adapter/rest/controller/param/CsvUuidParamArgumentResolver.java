package com.simulacert.adapter.rest.controller.param;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CsvUuidParamArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(CsvUuidParam.class)) {
            return false;
        }

        if (!List.class.isAssignableFrom(parameter.getParameterType())) {
            return false;
        }

        Type type = parameter.getGenericParameterType();
        if (!(type instanceof ParameterizedType pt)) {
            return false;
        }

        Type[] args = pt.getActualTypeArguments();
        return args.length == 1 && args[0] == UUID.class;
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        CsvUuidParam ann = parameter.getParameterAnnotation(CsvUuidParam.class);
        if (ann == null) return null;

        String paramName = ann.value();
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return Collections.emptyList();
        }

        String raw = request.getParameter(paramName);

        if (raw == null) {
            if (ann.required()) {
                throw new MissingServletRequestParameterException(paramName, ValueConstants.DEFAULT_NONE);
            }

            return Collections.emptyList();
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            if (ann.allowEmpty()) {
                return Collections.emptyList();
            }

            if (ann.required()) {
                throw new IllegalArgumentException("Query param '" + paramName + "' must not be blank");
            }

            return Collections.emptyList();
        }

        try {
            return Arrays.stream(trimmed.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(UUID::fromString)
                    .toList();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID in query param '" + paramName + "'");
        }
    }
}

