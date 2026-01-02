package br.com.simulaaws.exam.infrastructure.persistence.adapter;

import br.com.simulaaws.exam.application.port.out.ExamQueryPort;
import br.com.simulaaws.exam.infrastructure.persistence.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExamQueryAdapter implements ExamQueryPort {

    private final ExamRepository repository;

    @Override
    public boolean existsById(UUID examId) {
        return repository.existsById(examId);
    }
}
