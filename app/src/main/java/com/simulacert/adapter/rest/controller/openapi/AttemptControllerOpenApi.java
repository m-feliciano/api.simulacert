package com.simulacert.adapter.rest.controller.openapi;

import com.simulacert.attempt.application.dto.AnswerResponse;
import com.simulacert.attempt.application.dto.AttemptQuestionResponse;
import com.simulacert.attempt.application.dto.AttemptResponse;
import com.simulacert.attempt.application.dto.AttemptTimingResponse;
import com.simulacert.attempt.application.dto.StartAttemptRequest;
import com.simulacert.attempt.application.dto.SubmitAnswerRequest;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Tag(name = "Attempts", description = "Exam attempt management endpoints")
public interface
AttemptControllerOpenApi {

    @Operation(
            summary = "Start exam attempt",
            description = "Starts a new exam attempt for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attempt started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Not authorized - can only start attempts for yourself"),
            @ApiResponse(responseCode = "404", description = "Exam not found")
    })
    ResponseEntity<Void> startAttempt(@RequestBody @Valid StartAttemptRequest request);

    @Operation(summary = "Retake exam attempt",
            description = "Allows a user to retake an exam by starting a new attempt based on a previous attempt. The new attempt will have the same exam and user, but a new start time and reset score. This is useful for allowing users to retry exams while keeping a history of their attempts.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attempt started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Not authorized - can only start attempts for yourself"),
            @ApiResponse(responseCode = "404", description = "Exam not found")
    })
    AttemptResponse retakeAttempt(@PathVariable UUID attemptId);

    @Operation(
            summary = "Finish exam attempt",
            description = "Completes an exam attempt with the final score",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempt finished successfully",
                    content = @Content(schema = @Schema(implementation = AttemptResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid score value (must be between 0 and 100)"),
            @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    ResponseEntity<AttemptResponse> finishAttempt(@Parameter(description = "Attempt ID", required = true) @PathVariable UUID attemptId);

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
            @ApiResponse(responseCode = "404", description = "Attempt not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized - Admin role required")
    })
    ResponseEntity<AttemptResponse> getAttempt(@Parameter(description = "Attempt ID", required = true) @PathVariable UUID attemptId);

    @Operation(
            summary = "Get user attempts",
            description = "Retrieves all attempts for a specific user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User attempts retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized - can only view own attempts or require Admin role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    ResponseEntity<Page<AttemptResponse>> getAttemptsByUser(@Parameter(description = "User ID", required = true) @PathVariable UUID userId,
                                                            @PageableDefault(
                                                                    size = 5,
                                                                    sort = "startedAt",
                                                                    direction = Sort.Direction.DESC) Pageable pageable);

    @Operation(
            summary = "Get attempt questions",
            description = "Retrieves all questions associated with a specific attempt",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempt questions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    ResponseEntity<List<AttemptQuestionResponse>> getAttemptQuestions(@PathVariable UUID attemptId);

    @Operation(
            summary = "Submit answer for question",
            description = "Submits an answer for a specific question within an attempt",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Answer submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid answer data"),
            @ApiResponse(responseCode = "404", description = "Attempt or question not found")
    })
    void submitAnswer(
            @PathVariable UUID attemptId,
            @PathVariable UUID questionId,
            @RequestBody @Valid SubmitAnswerRequest request);

    @Operation(
            summary = "Delete answer for question",
            description = "Deletes the submitted answer for a specific question within an attempt",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Answer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attempt or question not found")
    })
    void deleteAnswer(@PathVariable UUID attemptId, @PathVariable UUID questionId);

    @Operation(
            summary = "Cancel exam attempt",
            description = "Cancels an ongoing exam attempt for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Attempt cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    void cancelAttempt(@Parameter(description = "Attempt ID", required = true) @PathVariable UUID attemptId);

    @Operation(
            summary = "Pause attempt timer",
            description = "Pauses the server-authoritative timer for an in-progress attempt",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempt paused",
                    content = @Content(schema = @Schema(implementation = AttemptTimingResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Attempt not found"),
            @ApiResponse(responseCode = "409", description = "Attempt not in progress")
    })
    ResponseEntity<AttemptTimingResponse> pauseAttempt(@PathVariable UUID attemptId);

    @Operation(
            summary = "Resume attempt timer",
            description = "Resumes the server-authoritative timer for a paused attempt",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempt resumed",
                    content = @Content(schema = @Schema(implementation = AttemptTimingResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Attempt not found"),
            @ApiResponse(responseCode = "409", description = "Attempt not paused or not in progress")
    })
    ResponseEntity<AttemptTimingResponse> resumeAttempt(@PathVariable UUID attemptId);

    @Operation(
            summary = "Attempt heartbeat",
            description = "Updates/returns timer state for an attempt (keep-alive)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Heartbeat OK",
                    content = @Content(schema = @Schema(implementation = AttemptTimingResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Attempt not found"),
            @ApiResponse(responseCode = "409", description = "Attempt not in progress")
    })
    ResponseEntity<AttemptTimingResponse> heartbeatAttempt(@PathVariable UUID attemptId);

    @Operation(
            summary = "Get answer for attempt",
            description = "Retrieves the submitted answer for a specific attempt.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Answer retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Answer not found")
    })
    List<AnswerResponse> getAnswer(
            @Parameter(description = "Attempt ID", required = true) @PathVariable UUID attemptId);
}

