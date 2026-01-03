package com.simulacert.exam.application.service;

import com.simulacert.exam.application.dto.request.CreateQuestionRequest;
import com.simulacert.exam.application.dto.response.QuestionResponse;
import com.simulacert.exam.application.mapper.QuestionMapper;
import com.simulacert.exam.application.port.in.QuestionUseCase;
import com.simulacert.exam.application.port.out.ExamRepositoryPort;
import com.simulacert.exam.application.port.out.QuestionRepositoryPort;
import com.simulacert.exam.domain.Question;
import com.simulacert.exam.domain.QuestionOption;
import com.simulacert.exam.infrastructure.persistence.repository.QuestionOptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService implements QuestionUseCase {

    private final QuestionRepositoryPort questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ExamRepositoryPort examRepository;
    private final QuestionMapper questionMapper;

    @Override
    public List<QuestionResponse> getQuestionsByExamId(UUID examId) {
        var questions = questionRepository.findByExamId(examId);
        return questionMapper.toResponseList(questions);
    }

    @Override
    public Page<QuestionResponse> getQuestionsByExamIdPaginated(UUID examId, Pageable pageable) {
        log.debug("Getting paginated questions for exam {} with pageable: {}", examId, pageable);
        Page<Question> questionsPage = questionRepository.findByExamIdPaginated(examId, pageable);
        return questionsPage.map(questionMapper::toResponse);
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

    @Override
    @Transactional
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        log.info("Creating question for exam: {}", request.examId());

        if (!examRepository.existsById(request.examId())) {
            log.warn("Exam not found: {}", request.examId());
            throw new IllegalArgumentException("Exam not found: " + request.examId());
        }

        Question question = Question.create(
                request.examId(),
                request.text(),
                request.domain(),
                request.difficulty()
        );

        Question saved = questionRepository.save(question);

        List<QuestionOption> options = request.options().stream()
                .map(opt -> QuestionOption.create(
                        saved,
                        opt.key(),
                        opt.text(),
                        opt.isCorrect()
                ))
                .toList();

        questionOptionRepository.saveAll(options);

        log.info("Question created with id: {} and {} options", saved.getId(), options.size());

        return questionMapper.toResponse(saved);
    }
}
