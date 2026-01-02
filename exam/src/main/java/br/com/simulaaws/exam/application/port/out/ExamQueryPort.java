package br.com.simulaaws.exam.application.port.out;

import java.util.UUID;

public interface ExamQueryPort {
    boolean existsById(UUID examId);
}
