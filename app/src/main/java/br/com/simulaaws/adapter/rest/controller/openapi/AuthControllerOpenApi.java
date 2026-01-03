package br.com.simulaaws.adapter.rest.controller.openapi;

import br.com.simulaaws.auth.application.dto.AuthResponse;
import br.com.simulaaws.auth.application.dto.ChangePasswordRequest;
import br.com.simulaaws.auth.application.dto.LoginRequest;
import br.com.simulaaws.auth.application.dto.RegisterRequest;
import br.com.simulaaws.auth.application.dto.UserResponse;
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

import java.util.UUID;

@Tag(name = "Authentication", description = "User authentication and management endpoints")
public interface AuthControllerOpenApi {

    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with USER role"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or email already exists"
            )
    })
    ResponseEntity<Void> register(@RequestBody RegisterRequest request);

    @Operation(
            summary = "User login",
            description = "Authenticates user and returns JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User account is inactive"
            )
    })
    ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request);

    @Operation(
            summary = "Get user by ID",
            description = "Retrieves user information by user ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId
    );

    @Operation(
            summary = "Get user by email",
            description = "Retrieves user information by email address. Admin only.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - Admin role required"
            )
    })
    ResponseEntity<UserResponse> getUserByEmail(
            @Parameter(description = "User email address", required = true)
            @PathVariable String email
    );

    @Operation(
            summary = "Change user password",
            description = "Allows user to change their own password",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Password changed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid old password"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    ResponseEntity<Void> changePassword(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            @RequestBody ChangePasswordRequest request
    );

    @Operation(
            summary = "Activate user account",
            description = "Activates a deactivated user account. Admin only.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User activated successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - Admin role required"
            )
    })
    ResponseEntity<Void> activateUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId
    );

    @Operation(
            summary = "Deactivate user account",
            description = "Deactivates an active user account. Admin only.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User deactivated successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized - Admin role required"
            )
    })
    ResponseEntity<Void> deactivateUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId
    );
}

