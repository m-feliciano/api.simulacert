package com.simulacert.adapter.rest.dto;

import java.util.UUID;

public record StartAttemptRequest(UUID userId, UUID examId, int questionCount) {
}
