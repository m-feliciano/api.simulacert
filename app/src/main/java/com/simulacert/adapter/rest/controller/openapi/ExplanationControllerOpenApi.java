package com.simulacert.adapter.rest.controller.openapi;

import com.simulacert.llm.application.dto.ExplanationResponse;
import com.simulacert.llm.application.dto.RequestExplanationCommand;
import com.simulacert.llm.application.dto.SubmitFeedbackCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Question Explanations", description = "AI-generated explanations for exam questions (Experimental)")
public interface ExplanationControllerOpenApi {

    @Operation(
            summary = "Request AI explanation for a question",
            description = "Generate an AI explanation for why the correct answer is correct and why incorrect options are wrong. " +
                          "This is an experimental feature and requires the exam attempt to be completed.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Explanation generated successfully",
                    content = @Content(schema = @Schema(implementation = ExplanationResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request or attempt not completed"),
            @ApiResponse(responseCode = "403", description = "Attempt does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Question or attempt not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    ResponseEntity<ExplanationResponse> requestExplanation(
            @Parameter(description = "Question ID") UUID questionId,
            @Valid RequestExplanationCommand command
    );

    @Operation(
            summary = "Submit feedback for an explanation",
            description = "Rate and provide feedback on the quality of an AI-generated explanation",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Feedback submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid rating or feedback"),
            @ApiResponse(responseCode = "404", description = "Explanation not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    ResponseEntity<Void> submitFeedback(
            @Valid SubmitFeedbackCommand command,
            @Parameter(description = "Explanation ID") UUID explanationId
    );
}

