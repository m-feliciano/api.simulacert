package com.simulacert.exam.infrastructure.persistence.repository;

import com.simulacert.exam.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @EntityGraph(attributePaths = "options")
    List<Question> findByExamId(UUID examId);

    Page<Question> findByExamId(UUID examId, Pageable pageable);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.examId = :examId")
    long countByExamId(@Param("examId") UUID examId);

    @Query("SELECT q.id FROM Question q WHERE q.examId = :examId")
    List<UUID> findIdsByExamId(@Param("examId") UUID examId);
}
