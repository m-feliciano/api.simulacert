package com.simulacert.exam.application.port.out;

import com.simulacert.exam.domain.Exam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamRepositoryPort {
    Optional<Exam> findById(UUID id);

    boolean existsById(UUID examId);

    List<Exam> findAll();

    Exam save(Exam exam);

    void deleteById(UUID id);

    boolean existsByTitle(String title);
}

