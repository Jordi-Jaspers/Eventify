package io.github.eventify.api.apikey.model.response;

import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.user.model.response.UserResponse;
import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response DTO for API key in list view.
 * Contains masked key - never includes full key.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "API key information with masked key value")
public class ApiKeyResponse implements PageableItemResource {

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
        description = "Masked API key showing only prefix and last 4 characters",
        example = "evt_******o5p6",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String maskedKey;

    @Schema(
        description = "API key scope",
        example = "USER",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ApiKeyScope scope;

    @Schema(
        description = "Owner information (user or organization)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ApiKeyOwnerResponse owner;

    @Schema(
        description = "User who created the key (only for organization keys)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private UserResponse createdBy;

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
        description = "Timestamp when the key was last used for authentication",
        example = "2026-01-08T15:45:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime lastUsedAt;

    @Schema(
        description = "Total number of API requests made with this key",
        example = "1542",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long totalRequests;

    @Schema(
        description = "Whether the key is expired",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isExpired;
}
