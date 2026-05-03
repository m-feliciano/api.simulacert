package com.simulacert.adapter.rest.controller.openapi;

import com.simulacert.review.application.dto.CreateReviewRequest;
import com.simulacert.review.application.dto.ReviewResponse;
import com.simulacert.review.application.dto.ReviewSummaryResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Reviews", description = "Manage attempt reviews")
public interface ReviewControllerOpenApi {

    @Operation(
            summary = "Create review",
            description = "Create a review for a completed attempt",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Review created successfully",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - attempt not found or validation failed"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Review already exists for this attempt"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            )
    })
    ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request);

    @Operation(
            summary = "Get review by attempt",
            description = "Retrieve review for a specific attempt",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Review found",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found for this attempt"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            )
    })
    ResponseEntity<ReviewResponse> getReviewByAttempt(
            @Parameter(description = "Attempt ID", required = true)
            @PathVariable("attemptId") UUID attemptId
    );

    @Operation(
            summary = "Get review summary for a user",
            description = "Returns aggregated review metrics for a given user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ReviewSummaryResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "Not authorized - can only view own summary or require Admin role"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    ResponseEntity<ReviewSummaryResponse> getSummaryByUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable("userId") UUID userId
    );
}

