package br.com.simulaaws.exam.adapter.rest.openapi;

import br.com.simulaaws.clients.exam.dto.QuestionResponse;
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

import java.util.List;
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
            summary = "Get questions by exam",
            description = "Retrieves all questions for a specific exam",
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
    ResponseEntity<List<QuestionResponse>> getQuestionsByExam(
            @Parameter(description = "Exam ID", required = true)
            @PathVariable UUID examId
    );
}

