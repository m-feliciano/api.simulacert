package com.simulacert.exam.application.port.out;

import java.util.List;
import java.util.UUID;

public interface QuestionQueryPort {
    List<UUID> findQuestionIdsByExamId(UUID examId);
}
