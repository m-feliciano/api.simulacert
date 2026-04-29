package com.simulacert.attempt.application.dto;

import com.simulacert.attempt.domain.Answer;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;


@Builder
public record AnswerResponse(
        UUID questionId,
        String selectedOption,
        Instant answeredAt
) {
    public static AnswerResponse from(Answer answer) {
        return AnswerResponse.builder()
                .questionId(answer.getQuestionId())
                .selectedOption(answer.getSelectedOption())
                .answeredAt(answer.getAnsweredAt())
                .build();
    }
}


