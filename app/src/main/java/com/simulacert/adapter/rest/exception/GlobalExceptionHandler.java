package com.simulacert.adapter.rest.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ErrorCode mapToErrorCode(String message) {
        if (message == null) {
            return ErrorCode.VALIDATION_ERROR;
        }

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

        return ErrorCode.VALIDATION_ERROR;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiErrorResponse handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.error("Illegal argument exception caught: ", ex);

        ErrorCode errorCode = mapToErrorCode(ex.getMessage());
        return new ApiErrorResponse(
                errorCode.name(),
                "This error may occur due to invalid input data or request parameters.",
                Instant.now(),
                request.getRequestURI()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            HttpClientErrorException.BadRequest.class,
    })
    public ApiErrorResponse handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        log.error("Illegal state exception caught: ", ex);

        ErrorCode errorCode = mapToErrorCode(ex.getMessage());
        return new ApiErrorResponse(
                errorCode.name(),
                "This error may occur due to invalid input data or request parameters.",
                Instant.now(),
                request.getRequestURI()
        );
    }

    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    @ExceptionHandler(UnsupportedOperationException.class)
    public ApiErrorResponse handleUnsupportedOperationException(
            UnsupportedOperationException ex,
            HttpServletRequest request
    ) {
        log.error("Unsupported operation exception caught: ", ex);

        return new ApiErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                "This operation is not supported.",
                Instant.now(),
                request.getRequestURI()
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiErrorResponse handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Global exception caught: ", ex);

        return new ApiErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                "An unexpected error occurred",
                Instant.now(),
                request.getRequestURI()
        );
    }
}

