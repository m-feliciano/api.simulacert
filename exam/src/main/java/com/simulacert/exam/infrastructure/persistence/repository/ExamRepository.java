package com.simulacert.exam.infrastructure.persistence.repository;

import com.simulacert.exam.domain.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {
    boolean existsByTitle(String title);

    Optional<Exam> findBySlug(String slug);
}
