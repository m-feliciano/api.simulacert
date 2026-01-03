package com.simulacert.exam.application.port.in;

import com.simulacert.exam.application.dto.request.CreateQuestionRequest;
import com.simulacert.exam.application.dto.response.QuestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface QuestionUseCase {

    List<QuestionResponse> getQuestionsByExamId(UUID examId);

    Page<QuestionResponse> getQuestionsByExamIdPaginated(UUID examId, Pageable pageable);

    QuestionResponse getQuestionById(UUID questionId);

    long countQuestionsByExamId(UUID examId);

    QuestionResponse createQuestion(CreateQuestionRequest request);
}