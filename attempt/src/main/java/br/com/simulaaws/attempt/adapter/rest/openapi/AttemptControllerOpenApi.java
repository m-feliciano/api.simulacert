package br.com.simulaaws.attempt.adapter.rest.openapi;

import br.com.simulaaws.attempt.application.dto.AttemptResponse;
import br.com.simulaaws.attempt.application.dto.StartAttemptRequest;
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
            summary = "Start new attempt",
            description = "Creates a new exam attempt for the authenticated user. User can only create attempts for themselves.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Attempt created successfully",
                    content = @Content(schema = @Schema(implementation = AttemptResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or attempt already exists"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User trying to create attempt for another user"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            )
    })
    ResponseEntity<AttemptResponse> startAttempt(@RequestBody StartAttemptRequest request);

    @Operation(
            summary = "Finish attempt",
            description = "Finalizes an exam attempt with a score. User can only finish their own attempts. Score must be between 0 and 100.",
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
                    description = "Invalid score or attempt already finished"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User trying to finish another user's attempt"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Attempt not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            )
    })
    ResponseEntity<AttemptResponse> finishAttempt(
            @Parameter(description = "Attempt ID", required = true)
            @PathVariable UUID attemptId,
            @Parameter(description = "Score (0-100)", required = true)
            @RequestParam int score
    );

    @Operation(
            summary = "Get attempt by ID",
            description = "Retrieves detailed information about a specific attempt. Admin only.",
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
            @Parameter(description = "Attempt ID", required = true)
            @PathVariable UUID attemptId
    );

    @Operation(
            summary = "Get attempts by user",
            description = "Retrieves all attempts for a specific user. User can only see their own attempts unless they are admin.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Attempts retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User trying to view another user's attempts"
            )
    })
    ResponseEntity<List<AttemptResponse>> getAttemptsByUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId
    );
}

