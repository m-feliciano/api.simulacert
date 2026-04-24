package com.simulacert.exam.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simulacert.exam.application.config.ImportProperties;
import com.simulacert.exam.application.dto.request.CreateExamRequest;
import com.simulacert.exam.application.dto.request.ExamImportDto;
import com.simulacert.exam.application.dto.request.UpdateExamRequest;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService implements ExamUseCase {

    private final ObjectMapper objectMapper;
    private final ExamRepositoryPort examRepository;
    private final QuestionRepositoryPort questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ExamMapper examMapper;
    private final ImportProperties importProperties;

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
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));

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
            throw new IllegalArgumentException("Exam not found: " + examId);
        }

        examRepository.deleteById(examId);

        log.info("Exam deleted: {}", examId);
    }

    public boolean hasMinimumQuestions(UUID examId) {
        return questionRepository.countByExamId(examId) >= 10;
    }

    @Override
    public ExamResponse getExamBySlug(String slug) {
        return examRepository.findBySlug(slug)
                .map(examMapper::toResponse)
                .orElse(null);
    }

    @SneakyThrows
    @Override
    public void importExamsFilesFromDirectory() {
        log.info("Starting directory-based exam import from: {}", importProperties.getInputDir());

        Path importPath = Paths.get(importProperties.getInputDir());
        Path processedPath = Paths.get(importProperties.getProcessedDir());

        Files.createDirectories(importPath);
        Files.createDirectories(processedPath);

        List<Path> files;
        try (Stream<Path> paths = Files.list(importPath)) {
            files = paths.filter(Files::isRegularFile).toList();
        }

        Map<Path, ExamImportDto> fileMap = new java.util.HashMap<>();
        for (Path path : files) {
            log.info("Reading file: {}", path);
            fileMap.put(path, objectMapper.readValue(path.toFile(), ExamImportDto.class));
        }

        if (fileMap.isEmpty()) {
            log.warn("No exam files found in directory: {}", importProperties.getInputDir());
            return;
        }

        List<ExamImportDto> exams = new ArrayList<>(fileMap.values());
        this.importExams(exams);

        for (Path source : fileMap.keySet()) {
            try {
                Path target = processedPath.resolve(source.getFileName());
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.warn("Failed to move file: {}", source.getFileName(), e);
            }
        }
    }

    @SneakyThrows
    @Transactional
    @Override
    public void importExamsFiles(List<MultipartFile> files) {
        log.info("Starting exam import for {} files", files.size());

        List<ExamImportDto> exams = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if (file.isEmpty() || fileName == null || !fileName.toLowerCase().endsWith(".json")) {
                log.warn("Invalid file: {}", fileName);
                continue;
            }

            ExamImportDto dto = objectMapper.readValue(file.getInputStream(), ExamImportDto.class);
            exams.add(dto);
        }

        if (exams.isEmpty()) {
            log.warn("No valid exam files to import");
            return;
        }

        this.importExams(exams);
        log.info("Successfully imported exam: {}", exams.size());
    }

    public void importExams(List<ExamImportDto> exams) {
        log.info("Starting bulk exam import for {} exams", exams.size());

        for (ExamImportDto importDto : exams) {
            if (examRepository.existsByTitle(importDto.title())) {
                log.warn("Exam with title '{}' already exists", importDto.title());
                continue;
            }

            log.info("Starting exam import: {}", importDto.title());

            Exam exam = examRepository.save(Exam.create(importDto.title(), importDto.description(), importDto.slug()));

            for (var qdto : importDto.questions()) {
                Question question = questionRepository.save(
                        Question.create(exam.getId(), qdto.text(), qdto.domain(), qdto.difficulty()));

                var options = qdto.options().stream()
                        .map(opt ->
                                QuestionOption.create(question, opt.key(), opt.text(), opt.correct()))
                        .toList();

                questionOptionRepository.saveAll(options);
            }

            log.info("Exam import completed: {} with {} questions", exam.getId(), importDto.questions().size());
        }
    }
}

