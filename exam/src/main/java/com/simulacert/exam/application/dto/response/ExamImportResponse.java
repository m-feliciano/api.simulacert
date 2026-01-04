package com.simulacert.exam.application.dto.response;

import java.util.UUID;

public record ExamImportResponse(
        UUID examId,
        String title,
        int questionsImported,
        String status
) {
}

