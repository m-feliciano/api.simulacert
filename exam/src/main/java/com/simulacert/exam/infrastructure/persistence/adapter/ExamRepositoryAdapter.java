package com.simulacert.exam.infrastructure.persistence.adapter;

import com.simulacert.exam.application.port.out.ExamRepositoryPort;
import com.simulacert.exam.domain.Exam;
import com.simulacert.exam.infrastructure.persistence.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ExamRepositoryAdapter implements ExamRepositoryPort {

    private final ExamRepository repository;

    @Override
    public Optional<Exam> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public List<Exam> findAll() {
        return repository.findAll();
    }

    @Override
    public Exam save(Exam exam) {
        return repository.save(exam);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByTitle(String title) {
        return repository.existsByTitle(title);
    }

    @Override
    public Optional<Exam> findBySlug(String slug) {
        return repository.findBySlug(slug);
    }
}


