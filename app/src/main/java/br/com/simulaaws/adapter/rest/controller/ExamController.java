package br.com.simulaaws.adapter.rest.controller;

import br.com.simulaaws.clients.exam.dto.ExamResponse;
import br.com.simulaaws.exam.application.dto.CreateExamRequest;
import br.com.simulaaws.exam.application.dto.UpdateExamRequest;
import br.com.simulaaws.exam.application.port.in.ExamUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamUseCase examUseCase;

    @GetMapping
    public ResponseEntity<List<ExamResponse>> getAllExams() {
        log.debug("Getting all exams");

        List<ExamResponse> exams = examUseCase.getAllExams();

        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ExamResponse> getExam(@PathVariable UUID examId) {
        log.debug("Getting exam {}", examId);

        ExamResponse response = examUseCase.getExamById(examId);

        if (response == null) {
            log.warn("Exam {} not found", examId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{examId}/exists")
    public ResponseEntity<Boolean> examExists(@PathVariable UUID examId) {
        log.debug("Checking if exam {} exists", examId);

        boolean exists = examUseCase.examExists(examId);

        return ResponseEntity.ok(exists);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamResponse> createExam(@Valid @RequestBody CreateExamRequest request) {
        log.info("Creating exam with title: {}", request.title());

        ExamResponse response = examUseCase.createExam(request);

        log.info("Exam created with id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{examId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamResponse> updateExam(
            @PathVariable UUID examId,
            @Valid @RequestBody UpdateExamRequest request) {
        log.info("Updating exam: {}", examId);

        try {
            ExamResponse response = examUseCase.updateExam(examId, request);
            log.info("Exam updated: {}", examId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Exam not found: {}", examId);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{examId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteExam(@PathVariable UUID examId) {
        log.info("Deleting exam: {}", examId);

        try {
            examUseCase.deleteExam(examId);
            log.info("Exam deleted: {}", examId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Exam not found: {}", examId);
            return ResponseEntity.notFound().build();
        }
    }
}

