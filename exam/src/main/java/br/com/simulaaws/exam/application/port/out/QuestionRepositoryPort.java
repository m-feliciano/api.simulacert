package br.com.simulaaws.exam.application.port.out;

import br.com.simulaaws.exam.domain.Question;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface QuestionRepositoryPort {
    List<Question> findByExamId(UUID examId);

    Question findById(UUID id);

    long countByExamId(UUID examId);

    @Query("select q.id from Question q where q.examId = :examId")
    List<UUID> findIdsByExamId(UUID examId);

    Question save(Question question);
}