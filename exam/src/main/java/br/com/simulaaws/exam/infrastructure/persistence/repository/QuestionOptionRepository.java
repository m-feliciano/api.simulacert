package br.com.simulaaws.exam.infrastructure.persistence.repository;

import br.com.simulaaws.exam.domain.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, UUID> {

    List<QuestionOption> findByQuestionId(UUID questionId);

    void deleteByQuestionId(UUID questionId);

    @Query("SELECT qo.optionKey FROM QuestionOption qo WHERE qo.questionId = :questionId AND qo.isCorrect = true")
    List<String> findCorrectOptionKeysByQuestionId(@Param("questionId") UUID questionId);
}