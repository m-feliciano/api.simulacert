package com.simulacert.exam.application.port.out;

import java.util.UUID;

public interface ExamQueryPort {
    boolean existsById(UUID examId);
}
