package io.github.eventify.api.apikey.model.response;

import io.github.eventify.api.user.model.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response DTO for creating an API key.
 * Contains the full key - only returned on creation.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response returned when creating a new API key")
public class ApiKeyCreationResponse {

    @Schema(
        description = "Unique identifier of the API key",
        example = "123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "Human-readable name of the API key",
        example = "Production Server",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "Full API key value - only shown once at creation",
        example = "evt_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String key;

    @Schema(
        description = "Last 4 characters of the key for identification",
        example = "o5p6",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String suffix;

    @Schema(
        description = "Timestamp when the key was created",
        example = "2026-01-08T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime createdAt;

    @Schema(
        description = "Timestamp when the key expires (null if no expiration)",
        example = "2027-01-08T10:30:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime expiresAt;

    @Schema(
        description = "User who created the key (only for organization keys)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private UserResponse createdBy;
}
