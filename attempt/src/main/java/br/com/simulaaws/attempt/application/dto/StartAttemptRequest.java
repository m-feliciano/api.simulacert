package br.com.simulaaws.attempt.application.dto;

import java.util.UUID;

public record StartAttemptRequest(UUID userId, UUID examId, int questionCount) {
}
