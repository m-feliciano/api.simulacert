package br.com.simulaaws.clients.exam.dto;

import java.util.UUID;

public record QuestionResponse(
        UUID id,
        UUID examId,
        String text,
        String domain,
        String difficulty
) {
}

