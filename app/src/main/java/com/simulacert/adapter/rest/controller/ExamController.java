package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.controller.openapi.ExamControllerOpenApi;
import com.simulacert.exam.application.dto.request.CreateExamRequest;
import com.simulacert.exam.application.dto.response.ExamResponse;
import com.simulacert.exam.application.dto.request.UpdateExamRequest;
import com.simulacert.exam.application.port.in.ExamUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController implements ExamControllerOpenApi {

    private final ExamUseCase examUseCase;

    @Override
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
    public ResponseEntity<Void> createExam(@Valid @RequestBody CreateExamRequest request) {
        log.info("Creating exam with title: {}", request.title());

        ExamResponse response = examUseCase.createExam(request);

        log.info("Exam created with id: {}", response.id());

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @Override
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

    @Override
    @DeleteMapping("/{examId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteExam(@PathVariable UUID examId) {
        log.info("Deleting exam: {}", examId);

        examUseCase.deleteExam(examId);
        log.info("Exam deleted: {}", examId);
        return ResponseEntity.noContent().build();
    }
}



