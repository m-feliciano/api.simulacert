package br.com.simulaaws.exam.infrastructure.persistence.adapter;

import br.com.simulaaws.exam.application.port.out.QuestionRepositoryPort;
import br.com.simulaaws.exam.domain.Question;
import br.com.simulaaws.exam.infrastructure.persistence.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class QuestionRepositoryAdapter implements QuestionRepositoryPort {

    private final QuestionRepository repository;

    @Override
    public List<Question> findByExamId(UUID examId) {
        return repository.findByExamId(examId);
    }

    @Override
    public Question findById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public long countByExamId(UUID examId) {
        return repository.countByExamId(examId);
    }
}
