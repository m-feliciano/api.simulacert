package com.simulacert.exam.application.service;

import com.simulacert.exam.application.dto.request.CreateExamRequest;
import com.simulacert.exam.application.dto.request.ExamImportDto;
import com.simulacert.exam.application.dto.request.UpdateExamRequest;
import com.simulacert.exam.application.dto.response.ExamImportResponse;
import com.simulacert.exam.application.dto.response.ExamResponse;
import com.simulacert.exam.application.mapper.ExamMapper;
import com.simulacert.exam.application.port.in.ExamUseCase;
import com.simulacert.exam.application.port.out.ExamRepositoryPort;
import com.simulacert.exam.application.port.out.QuestionRepositoryPort;
import com.simulacert.exam.domain.Exam;
import com.simulacert.exam.domain.Question;
import com.simulacert.exam.domain.QuestionOption;
import com.simulacert.exam.infrastructure.persistence.repository.QuestionOptionRepository;
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
    private final QuestionOptionRepository questionOptionRepository;
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

        Exam exam = Exam.create(request.title(), request.description(), request.slug());
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

    @Override
    @Transactional
    public ExamImportResponse importExam(ExamImportDto examImportDto) {
        log.info("Starting exam import: {}", examImportDto.title());

        // verifica se ja tem um exam com esse titulo
        if (examRepository.existsByTitle(examImportDto.title())) {
            log.warn("Exam with title '{}' already exists", examImportDto.title());
            return new ExamImportResponse(
                    null,
                    examImportDto.title(),
                    0,
                    "DUPLICATE_TITLE"
            );
        }

        Exam exam = Exam.create(examImportDto.title(), examImportDto.description(), examImportDto.slug());
        Exam savedExam = examRepository.save(exam);
        log.info("Exam created with id: {}", savedExam.getId());

        int questionsImported = 0;

        for (var questionDto : examImportDto.questions()) {
            Question question = Question.create(
                    savedExam.getId(),
                    questionDto.text(),
                    questionDto.domain(),
                    questionDto.difficulty()
            );

            Question savedQuestion = questionRepository.save(question);

            List<QuestionOption> options = questionDto.options().stream()
                    .map(opt -> QuestionOption.create(
                            savedQuestion,
                            opt.key(),
                            opt.text(),
                            opt.correct()
                    ))
                    .toList();

            questionOptionRepository.saveAll(options);
            questionsImported++;

            if (questionsImported % 100 == 0) {
                log.info("Imported {} questions for exam {}", questionsImported, savedExam.getId());
            }
        }

        log.info("Exam import completed: {} with {} questions", savedExam.getId(), questionsImported);

        return new ExamImportResponse(
                savedExam.getId(),
                savedExam.getTitle(),
                questionsImported,
                "SUCCESS"
        );
    }

    @Override
    public ExamResponse getExamBySlug(String slug) {
        return examRepository.findBySlug(slug)
                .map(examMapper::toResponse)
                .orElse(null);
    }

    private Long countTotalQuestions(UUID id) {
        return questionRepository.countByExamId(id);
    }
}

