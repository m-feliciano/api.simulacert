package br.com.simulaaws.exam.infrastructure.persistence.repository;

import br.com.simulaaws.exam.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByExamId(UUID examId);

    Page<Question> findByExamId(UUID examId, Pageable pageable);

    long countByExamId(UUID examId);

    @Query("SELECT q.id FROM Question q WHERE q.examId = :examId")
    List<UUID> findIdsByExamId(@Param("examId") UUID examId);
}
