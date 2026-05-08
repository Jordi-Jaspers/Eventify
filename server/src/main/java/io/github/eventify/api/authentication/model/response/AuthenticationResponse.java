package io.github.eventify.api.authentication.model.response;

import io.github.eventify.api.authentication.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * The response of login and registration endpoints.
 */
@Data
@NoArgsConstructor
public class AuthenticationResponse {

    @Schema(
        description = "User's first name",
        example = "John",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String firstName;

    @Schema(
        description = "User's last name",
        example = "Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String lastName;

    @Schema(
        description = "User's email address",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    @Schema(
        description = "User's system role",
        example = "USER",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Role role;

    @Schema(
        description = "JWT access token for API authentication",
        example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String accessToken;

    @Schema(
        description = "JWT refresh token for obtaining new access tokens",
        example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String refreshToken;

    @Schema(
        description = "Timestamp when the access token expires",
        example = "2026-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private ZonedDateTime expiresAt;

    @Schema(
        description = "Whether the user account is enabled",
        example = "true",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private boolean enabled;

    @Schema(
        description = "Whether the user's email has been validated",
        example = "true",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private boolean validated;

}
