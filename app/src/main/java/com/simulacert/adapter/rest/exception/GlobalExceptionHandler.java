package com.simulacert.adapter.rest.exception;

import com.simulacert.exception.ForbiddenException;
import com.simulacert.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ApiErrorResponse build(ErrorCode errorCode, HttpServletRequest request) {
        return new ApiErrorResponse(
                errorCode.name(),
                errorCode.defaultMessage(),
                Instant.now(),
                request.getRequestURI()
        );
    }

    private static ResponseEntity<ApiErrorResponse> buildResponseEntity(ErrorCode errorCode, HttpServletRequest request) {
        ApiErrorResponse response = build(errorCode, request);
        return ResponseEntity.status(errorCode.httpStatus()).body(response);
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
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.error("Illegal state exception caught: ", ex);
        ErrorCode errorCode = mapToErrorCode(ex.getMessage());
        return buildResponseEntity(errorCode, request);
    }

    @ExceptionHandler({
            ForbiddenException.class
    })
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        log.error("Forbidden exception caught: ", ex);
        return buildResponseEntity(ErrorCode.FORBIDDEN, request);
    }

    @ExceptionHandler({
            UnauthorizedException.class
    })
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        log.error("Unauthorized exception caught: ", ex);
        return buildResponseEntity(ErrorCode.UNAUTHORIZED, request);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedOperationException(
            UnsupportedOperationException ex,
            HttpServletRequest request
    ) {
        log.error("Unsupported operation exception caught: ", ex);
        return buildResponseEntity(ErrorCode.NOT_IMPLEMENTED, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Global exception caught: ", ex);
        return buildResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR, request);
    }
}

