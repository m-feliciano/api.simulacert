package com.simulacert.adapter.rest.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ATTEMPT_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "Attempt already in progress."),
    ATTEMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "Attempt not found."),
    EXAM_NOT_FOUND(HttpStatus.NOT_FOUND, "Exam not found."),
    INVALID_QUESTION_COUNT(HttpStatus.BAD_REQUEST, "Invalid question count."),
    INSUFFICIENT_QUESTIONS(HttpStatus.BAD_REQUEST, "Insufficient questions."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "This error may occur due to invalid input data or request parameters."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "You do not have permission to perform this action."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "You don't have permission to perform this action."),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "This operation is not supported."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
