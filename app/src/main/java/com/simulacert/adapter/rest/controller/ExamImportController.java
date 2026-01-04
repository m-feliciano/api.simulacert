package com.simulacert.adapter.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simulacert.adapter.rest.controller.openapi.ExamImportControllerOpenApi;
import com.simulacert.config.ImportProperties;
import com.simulacert.exam.application.dto.request.ExamImportDto;
import com.simulacert.exam.application.dto.response.ExamImportResponse;
import com.simulacert.exam.application.port.in.ExamUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/api/v1/exams/import")
@RequiredArgsConstructor
public class ExamImportController implements ExamImportControllerOpenApi {

    private final ExamUseCase examUseCase;
    private final ObjectMapper objectMapper;
    private final ImportProperties importProperties;

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExamImportResponse>> importExams(@RequestParam("files") List<MultipartFile> files) {
        log.info("Starting exam import for {} files", files.size());

        List<ExamImportResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if (file.isEmpty() || fileName == null || !fileName.endsWith(".json")) {
                log.warn("Invalid file: {}", fileName);
                responses.add(new ExamImportResponse(
                        null,
                        fileName != null ? fileName : "unknown",
                        0,
                        "INVALID_FILE"
                ));
                continue;
            }

            try {
                ExamImportDto examImportDto = objectMapper.readValue(file.getInputStream(), ExamImportDto.class);
                ExamImportResponse response = examUseCase.importExam(examImportDto);
                responses.add(response);
                log.info("Successfully imported exam: {}", response.title());
            } catch (Exception e) {
                log.error("Error importing file: {}", fileName, e);
                responses.add(new ExamImportResponse(
                        null,
                        fileName,
                        0,
                        "ERROR: " + e.getMessage()
                ));
            }
        }

        log.info("Import completed. Total: {}, Success: {}",
                responses.size(),
                responses.stream().filter(r -> "SUCCESS".equals(r.status())).count());

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @Override
    @PostMapping("/directory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExamImportResponse>> importFromDirectory() {
        log.info("Starting directory-based exam import from: {}", importProperties.getInputDir());

        Path importPath = Paths.get(importProperties.getInputDir());
        Path processedPath = Paths.get(importProperties.getProcessedDir());

        try {
            Files.createDirectories(importPath);
            Files.createDirectories(processedPath);
        } catch (IOException e) {
            log.error("Failed to create directories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        List<ExamImportResponse> responses = new ArrayList<>();

        try (Stream<Path> files = Files.list(importPath)) {
            files.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            log.info("Processing file: {}", path.getFileName());

                            ExamImportDto examImportDto = objectMapper.readValue(path.toFile(), ExamImportDto.class);
                            ExamImportResponse response = examUseCase.importExam(examImportDto);
                            responses.add(response);

                            Path targetPath = processedPath.resolve(path.getFileName());
                            Files.move(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            log.info("Moved file to: {}", targetPath);

                        } catch (Exception e) {
                            log.error("Error processing file: {}", path.getFileName(), e);
                            responses.add(new ExamImportResponse(
                                    null,
                                    path.getFileName().toString(),
                                    0,
                                    "ERROR: " + e.getMessage()
                            ));
                        }
                    });
        } catch (IOException e) {
            log.error("Error reading directory", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        log.info("Directory import completed. Total: {}, Success: {}",
                responses.size(),
                responses.stream().filter(r -> "SUCCESS".equals(r.status())).count());

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
}

