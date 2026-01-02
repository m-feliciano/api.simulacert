package br.com.simulaaws.exam.application.port.in;

import br.com.simulaaws.exam.application.dto.request.CreateQuestionRequest;
import br.com.simulaaws.exam.application.dto.response.QuestionResponse;

import java.util.List;
import java.util.UUID;

public interface QuestionUseCase {

    List<QuestionResponse> getQuestionsByExamId(UUID examId);

    QuestionResponse getQuestionById(UUID questionId);

    long countQuestionsByExamId(UUID examId);

    QuestionResponse createQuestion(CreateQuestionRequest request);
}