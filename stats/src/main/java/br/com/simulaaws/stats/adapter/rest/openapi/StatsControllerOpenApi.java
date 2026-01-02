package br.com.simulaaws.stats.adapter.rest.openapi;

import br.com.simulaaws.stats.application.dto.AttemptHistoryItemDto;
import br.com.simulaaws.stats.application.dto.AwsDomainStatsDto;
import br.com.simulaaws.stats.application.dto.UserStatsDto;
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

@Tag(name = "Statistics", description = "User statistics and analytics endpoints")
public interface StatsControllerOpenApi {

    @Operation(
            summary = "Get user statistics",
            description = "Retrieves overall statistics for a user (total attempts, average score, etc). User can only view their own stats unless they are admin.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserStatsDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User trying to view another user's statistics"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            )
    })
    ResponseEntity<UserStatsDto> getUserStatistics(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId
    );

    @Operation(
            summary = "Get attempt history",
            description = "Retrieves the complete history of attempts for a user. User can only view their own history unless they are admin.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Attempt history retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User trying to view another user's history"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            )
    })
    ResponseEntity<List<AttemptHistoryItemDto>> getAttemptHistory(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId
    );

    @Operation(
            summary = "Get performance by AWS domain",
            description = "Retrieves performance statistics grouped by AWS domain (e.g., Compute, Storage, Networking). User can only view their own stats unless they are admin.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Domain statistics retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User trying to view another user's domain stats"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            )
    })
    ResponseEntity<List<AwsDomainStatsDto>> getPerformanceByDomain(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId
    );
}

