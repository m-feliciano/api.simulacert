package com.simulacert.attempt.application.dto;

import com.simulacert.attempt.domain.Answer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;


@Builder
public record AnswerResponse(
        @Schema(description = "Question ID", example = "question-uuid-here") UUID questionId,
        @Schema(description = "Selected Option", example = "A") String selectedOption,
        @Schema(description = "Answered At", example = "2026-05-23T20:13:58Z") Instant answeredAt
) {
    public static AnswerResponse from(Answer answer) {
        return AnswerResponse.builder()
                .questionId(answer.getQuestionId())
                .selectedOption(answer.getSelectedOption())
                .answeredAt(answer.getAnsweredAt())
                .build();
    }
}


