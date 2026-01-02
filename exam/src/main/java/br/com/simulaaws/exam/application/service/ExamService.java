package br.com.simulaaws.exam.application.service;

import br.com.simulaaws.exam.application.dto.request.CreateExamRequest;
import br.com.simulaaws.exam.application.dto.response.ExamResponse;
import br.com.simulaaws.exam.application.dto.request.UpdateExamRequest;
import br.com.simulaaws.exam.application.mapper.ExamMapper;
import br.com.simulaaws.exam.application.port.in.ExamUseCase;
import br.com.simulaaws.exam.application.port.out.ExamRepositoryPort;
import br.com.simulaaws.exam.application.port.out.QuestionRepositoryPort;
import br.com.simulaaws.exam.domain.Exam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService implements ExamUseCase {

    private final ExamRepositoryPort examRepository;
    private final QuestionRepositoryPort questionRepository;
    private final ExamMapper examMapper;

    @Override
    public ExamResponse getExamById(UUID examId) {
        return examRepository.findById(examId)
                .map(examMapper::toResponse)
                .orElse(null);
    }

    @Override
    public boolean examExists(UUID examId) {
        return examRepository.existsById(examId);
    }

    @Override
    public List<ExamResponse> getAllExams() {
        return examRepository.findAll()
                .stream()
                .map(examMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ExamResponse createExam(CreateExamRequest request) {
        log.info("Creating exam with title: {}", request.title());

        Exam exam = Exam.create(request.title(), request.description());
        Exam savedExam = examRepository.save(exam);

        log.info("Exam created with id: {}", savedExam.getId());

        return examMapper.toResponse(savedExam);
    }

    @Override
    @Transactional
    public ExamResponse updateExam(UUID examId, UpdateExamRequest request) {
        log.info("Updating exam: {}", examId);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> {
                    log.warn("Exam not found: {}", examId);
                    return new IllegalArgumentException("Exam not found: " + examId);
                });

        exam.update(request.title(), request.description());
        Exam updatedExam = examRepository.save(exam);

        log.info("Exam updated: {}", examId);

        return examMapper.toResponse(updatedExam);
    }

    @Override
    @Transactional
    public void deleteExam(UUID examId) {
        log.info("Deleting exam: {}", examId);

        if (!examRepository.existsById(examId)) {
            log.warn("Exam not found: {}", examId);
            throw new IllegalArgumentException("Exam not found: " + examId);
        }

        examRepository.deleteById(examId);

        log.info("Exam deleted: {}", examId);
    }

    public boolean hasMinimumQuestions(UUID examId) {
        return questionRepository.countByExamId(examId) >= 10;
    }
}

