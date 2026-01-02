package br.com.simulaaws.exam.infrastructure.persistence.repository;

import br.com.simulaaws.exam.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByExamId(UUID examId);

    long countByExamId(UUID examId);
}


