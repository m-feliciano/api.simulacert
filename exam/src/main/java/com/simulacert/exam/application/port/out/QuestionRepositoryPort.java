package com.simulacert.exam.application.port.out;

import com.simulacert.exam.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface QuestionRepositoryPort {
    List<Question> findByExamId(UUID examId);

    Page<Question> findByExamIdPaginated(UUID examId, Pageable pageable);

    Question findById(UUID id);

    long countByExamId(UUID examId);

    @Query("select q.id from Question q where q.examId = :examId")
    List<UUID> findIdsByExamId(UUID examId);

    Question save(Question question);
}