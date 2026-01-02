package br.com.simulaaws.adapter.rest.controller.openapi;

import br.com.simulaaws.clients.exam.dto.ExamResponse;
import br.com.simulaaws.exam.application.dto.CreateExamRequest;
import br.com.simulaaws.exam.application.dto.UpdateExamRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Tag(name = "Exams", description = "Exam management endpoints")
public interface ExamControllerOpenApi {

    @Operation(summary = "Get all exams", description = "Retrieves a list of all available exams")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of exams retrieved successfully")
    })
    ResponseEntity<List<ExamResponse>> getAllExams();

    @Operation(summary = "Get exam by ID", description = "Retrieves detailed information about a specific exam")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exam found", content = @Content(schema = @Schema(implementation = ExamResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exam not found")
    })
    ResponseEntity<ExamResponse> getExam(@Parameter(description = "Exam ID", required = true) @PathVariable UUID examId);

    @Operation(summary = "Check if exam exists", description = "Verifies if an exam exists by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Existence check completed")
    })
    ResponseEntity<Boolean> examExists(@Parameter(description = "Exam ID", required = true) @PathVariable UUID examId);

    @Operation(summary = "Create new exam", description = "Creates a new exam. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exam created successfully", content = @Content(schema = @Schema(implementation = ExamResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Not authorized - Admin role required")
    })
    ResponseEntity<ExamResponse> createExam(@RequestBody CreateExamRequest request);

    @Operation(summary = "Update exam", description = "Updates an existing exam. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exam updated successfully", content = @Content(schema = @Schema(implementation = ExamResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exam not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized - Admin role required")
    })
    ResponseEntity<ExamResponse> updateExam(@Parameter(description = "Exam ID", required = true) @PathVariable UUID examId, @RequestBody UpdateExamRequest request);

    @Operation(summary = "Delete exam", description = "Deletes an exam. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Exam deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Exam not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized - Admin role required")
    })
    ResponseEntity<Void> deleteExam(@Parameter(description = "Exam ID", required = true) @PathVariable UUID examId);
}

