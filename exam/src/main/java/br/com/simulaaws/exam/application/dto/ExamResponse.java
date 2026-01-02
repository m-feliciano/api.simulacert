package br.com.simulaaws.exam.application.dto;

import java.util.UUID;

public record ExamResponse(
        UUID id,
        String title,
        String description
) {
}

