package br.com.simulaaws.adapter.rest.controller.openapi;

import br.com.simulaaws.adapter.rest.dto.AttemptResponse;
import br.com.simulaaws.adapter.rest.dto.StartAttemptRequest;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Attempts", description = "Exam attempt management endpoints")
public interface AttemptControllerOpenApi {

    @Operation(
            summary = "Start exam attempt",
            description = "Starts a new exam attempt for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Attempt started successfully",
                    content = @Content(schema = @Schema(implementation = AttemptResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - can only start attempts for yourself"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exam not found"
            )
    })
    ResponseEntity<Void> startAttempt(@RequestBody StartAttemptRequest request);

    @Operation(
            summary = "Finish exam attempt",
            description = "Completes an exam attempt with the final score",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Attempt finished successfully",
                    content = @Content(schema = @Schema(implementation = AttemptResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid score value (must be between 0 and 100)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Attempt not found"
            )
    })
    ResponseEntity<AttemptResponse> finishAttempt(
            @Parameter(description = "Attempt ID", required = true) @PathVariable UUID attemptId,
            @Parameter(description = "Final score (0-100)", required = true) @RequestParam int score
    );

    @Operation(
            summary = "Get attempt by ID",
            description = "Retrieves details of a specific attempt. Admin only.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Attempt found",
                    content = @Content(schema = @Schema(implementation = AttemptResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Attempt not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - Admin role required"
            )
    })
    ResponseEntity<AttemptResponse> getAttempt(
            @Parameter(description = "Attempt ID", required = true) @PathVariable UUID attemptId
    );

    @Operation(
            summary = "Get user attempts",
            description = "Retrieves all attempts for a specific user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User attempts retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - can only view own attempts or require Admin role"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    ResponseEntity<List<AttemptResponse>> getAttemptsByUser(
            @Parameter(description = "User ID", required = true) @PathVariable UUID userId
    );
}

