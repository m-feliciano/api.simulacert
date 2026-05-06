package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.controller.openapi.ExamImportControllerOpenApi;
import com.simulacert.exam.application.port.in.ExamUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/exams/import")
@RequiredArgsConstructor
public class ExamImportController implements ExamImportControllerOpenApi {

    private final ExamUseCase examUseCase;

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public void importExams(@RequestParam("files") List<MultipartFile> files) {
        examUseCase.importExamsFiles(files);
    }
}

