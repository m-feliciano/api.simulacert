package com.simulacert.adapter.rest.exception;

import com.simulacert.exception.ForbiddenException;
import com.simulacert.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ApiErrorResponse buildResponse(ErrorCode errorCode, HttpServletRequest request) {
        return new ApiErrorResponse(
                errorCode.name(),
                errorCode.getDefaultMessage(),
                Instant.now(),
                request.getRequestURI()
        );
    }

    private static ErrorCode mapToErrorCode(String message) {
        if (message != null) {
            if (message.contains("Attempt not found")) {
                return ErrorCode.ATTEMPT_NOT_FOUND;
            }
            if (message.contains("Exam not found")) {
                return ErrorCode.EXAM_NOT_FOUND;
            }
            if (message.contains("questionCount must be between")) {
                return ErrorCode.INVALID_QUESTION_COUNT;
            }
            if (message.contains("Exam has only")) {
                return ErrorCode.INSUFFICIENT_QUESTIONS;
            }
            if (message.contains("Cannot finish an attempt")) {
                return ErrorCode.ATTEMPT_ALREADY_IN_PROGRESS;
            }
        }

        return ErrorCode.VALIDATION_ERROR;
    }

    @ExceptionHandler({
            HttpClientErrorException.BadRequest.class,
            IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBadRequest(Exception ex, HttpServletRequest request) {
        log.error("Illegal state exception caught: ", ex);
        ErrorCode errorCode = mapToErrorCode(ex.getMessage());
        return buildResponse(errorCode, request);
    }

    @ExceptionHandler({
            ForbiddenException.class
    })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        log.error("Forbidden exception caught: ", ex);
        return buildResponse(ErrorCode.FORBIDDEN, request);
    }

    @ExceptionHandler({
            UnauthorizedException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        log.error("Unauthorized exception caught: ", ex);
        return buildResponse(ErrorCode.UNAUTHORIZED, request);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ApiErrorResponse handleUnsupportedOperationException(Exception ex, HttpServletRequest request) {
        log.error("Unsupported operation exception caught: ", ex);
        return buildResponse(ErrorCode.NOT_IMPLEMENTED, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Global exception caught: ", ex);
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNoResourceFound(Exception ignored, HttpServletRequest request) {
        log.error("Resource not found: {}", request.getRequestURI());

        return new ApiErrorResponse(
                "NOT_FOUND",
                "Resource not found",
                Instant.now(),
                request.getRequestURI()
        );
    }
}

