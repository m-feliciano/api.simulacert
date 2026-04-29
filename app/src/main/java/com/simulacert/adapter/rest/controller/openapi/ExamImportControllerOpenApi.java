package com.simulacert.adapter.rest.controller.openapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Exam Import", description = "Import exams in bulk")
public interface ExamImportControllerOpenApi {

    @Operation(
            summary = "Import exams from JSON files",
            description = "Upload multiple JSON files to import exams with questions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exams imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    })
    void importExams(@RequestParam("files") List<MultipartFile> files);

    @Operation(
            summary = "Import exams from directory",
            description = "Import all JSON files from 'fazer' directory and move to 'feito' after processing"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exams imported successfully from directory"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)"),
            @ApiResponse(responseCode = "500", description = "Error accessing directory")
    })
    void importFromDirectory();
}

