package com.simulacert.adapter.rest.controller.openapi;

import com.simulacert.exam.application.dto.request.CreateQuestionRequest;
import com.simulacert.exam.application.dto.response.QuestionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Questions", description = "Question retrieval endpoints")
public interface QuestionControllerOpenApi {

    @Operation(
            summary = "Get question by ID",
            description = "Retrieves detailed information about a specific question",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Question found",
                    content = @Content(schema = @Schema(implementation = QuestionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Question not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            )
    })
    ResponseEntity<QuestionResponse> getQuestion(
            @Parameter(description = "Question ID", required = true)
            @PathVariable UUID questionId
    );

    @Operation(
            summary = "Get questions by exam (paginated)",
            description = "Retrieves questions for a specific exam with pagination support",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Questions retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            )
    })
    ResponseEntity<Page<QuestionResponse>> getQuestionsByExam(
            @Parameter(description = "Exam ID", required = true)
            @PathVariable UUID examId,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable
    );

    @Operation(
            summary = "Create a new question",
            description = "Creates a new question for an exam")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Question created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            )
    })
    ResponseEntity<Void> createQuestion(@Valid @RequestBody CreateQuestionRequest request);
}
