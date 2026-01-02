package br.com.simulaaws.exam.application.dto.response;

import java.util.List;
import java.util.UUID;

public record QuestionResponse(
        UUID id,
        UUID examId,
        String text,
        String domain,
        String difficulty,
        List<QuestionOptionResponse> options
) {
}

