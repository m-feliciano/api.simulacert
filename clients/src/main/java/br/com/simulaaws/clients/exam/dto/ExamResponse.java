package br.com.simulaaws.clients.exam.dto;

import java.util.UUID;

public record ExamResponse(
        UUID id,
        String title,
        String description
) {
}

