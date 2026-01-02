package br.com.simulaaws.exam.application.service;

import br.com.simulaaws.clients.exam.dto.QuestionResponse;
import br.com.simulaaws.exam.application.mapper.QuestionMapper;
import br.com.simulaaws.exam.application.port.in.QuestionUseCase;
import br.com.simulaaws.exam.application.port.out.QuestionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionService implements QuestionUseCase {

    private final QuestionRepositoryPort questionRepository;
    private final QuestionMapper questionMapper;

    @Override
    public List<QuestionResponse> getQuestionsByExamId(UUID examId) {
        var questions = questionRepository.findByExamId(examId);
        return questionMapper.toResponseList(questions);
    }

    @Override
    public QuestionResponse getQuestionById(UUID questionId) {
        var question = questionRepository.findById(questionId);
        return question != null ? questionMapper.toResponse(question) : null;
    }

    @Override
    public long countQuestionsByExamId(UUID examId) {
        return questionRepository.countByExamId(examId);
    }
}

