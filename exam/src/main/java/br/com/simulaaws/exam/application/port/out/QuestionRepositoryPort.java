package br.com.simulaaws.exam.application.port.out;

import br.com.simulaaws.exam.domain.Question;

import java.util.List;
import java.util.UUID;

public interface QuestionRepositoryPort {
    List<Question> findByExamId(UUID examId);

    Question findById(UUID id);

    long countByExamId(UUID examId);
}

