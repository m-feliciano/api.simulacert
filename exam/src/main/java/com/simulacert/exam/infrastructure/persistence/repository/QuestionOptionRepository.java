package com.simulacert.exam.infrastructure.persistence.repository;

import com.simulacert.exam.domain.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, UUID> {

    List<QuestionOption> findByQuestionId(UUID questionId);
}