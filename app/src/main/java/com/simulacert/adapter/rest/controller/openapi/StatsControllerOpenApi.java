package com.simulacert.adapter.rest.controller.openapi;

import com.simulacert.stats.application.dto.AttemptHistoryItemDto;
import com.simulacert.stats.application.dto.AwsDomainStatsDto;
import com.simulacert.stats.application.dto.UserStatsDto;
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

@Tag(name = "Statistics", description = "User statistics and performance analytics endpoints")
public interface StatsControllerOpenApi {

    @Operation(
            summary = "Get user statistics",
            description = "Retrieves overall statistics for a specific user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserStatsDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - can only view own statistics or require Admin role"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    ResponseEntity<UserStatsDto> getUserStatistics(
            @Parameter(description = "User ID", required = true) @PathVariable UUID userId
    );

    @Operation(
            summary = "Get attempt history",
            description = "Retrieves the history of all attempts for a specific user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Attempt history retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - can only view own history or require Admin role"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    ResponseEntity<List<AttemptHistoryItemDto>> getAttemptHistory(
            @Parameter(description = "User ID", required = true) @PathVariable UUID userId
    );

    @Operation(
            summary = "Get performance by AWS domain",
            description = "Retrieves user performance statistics grouped by AWS service domains",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Domain statistics retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - can only view own statistics or require Admin role"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    ResponseEntity<List<AwsDomainStatsDto>> getPerformanceByDomain(
            @Parameter(description = "User ID", required = true) @PathVariable UUID userId
    );
}

