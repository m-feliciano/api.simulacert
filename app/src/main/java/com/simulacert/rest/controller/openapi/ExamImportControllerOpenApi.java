package com.simulacert.rest.controller.openapi;

import com.simulacert.rest.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
            @ApiResponse(responseCode = "400", description = "Invalid file format", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    void importExams(@RequestParam("files") List<MultipartFile> files);
}

