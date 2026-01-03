package com.simulacert.exam.infrastructure.persistence.adapter;

import com.simulacert.exam.application.port.out.QuestionQueryPort;
import com.simulacert.exam.infrastructure.persistence.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class QuestionQueryAdapter implements QuestionQueryPort {

    private final QuestionRepository repository;

    @Override
    public List<UUID> findQuestionIdsByExamId(UUID examId) {
        return repository.findIdsByExamId(examId);
    }
}
