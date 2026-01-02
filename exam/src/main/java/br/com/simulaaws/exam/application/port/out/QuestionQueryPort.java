package br.com.simulaaws.exam.application.port.out;

import java.util.List;
import java.util.UUID;

public interface QuestionQueryPort {
    List<UUID> findQuestionIdsByExamId(UUID examId);
}
