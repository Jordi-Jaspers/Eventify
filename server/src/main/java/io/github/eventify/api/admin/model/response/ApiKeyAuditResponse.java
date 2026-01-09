package io.github.eventify.api.admin.model.response;

import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.user.model.response.UserResponse;
import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response object for admin API key audit log entry.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ApiKeyAuditResponse implements PageableItemResource {

    @Schema(
        description = "Audit record ID",
        example = "123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "API key prefix (scope prefix + suffix)",
        example = "evt_****7890",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String keyPrefix;

    @Schema(
        description = "API key name",
        example = "Production Server",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String keyName;

    @Schema(
        description = "API key scope",
        example = "USER",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ApiKeyScope scope;

    @Schema(
        description = "Owner name",
        example = "John Doe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String ownerName;

    @Schema(
        description = "Owner email",
        example = "john@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String ownerEmail;

    @Schema(
        description = "Organization name (for org keys)",
        example = "Acme Corp",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String organizationName;

    @Schema(
        description = "When the key was originally created",
        example = "2024-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime createdAt;

    @Schema(
        description = "User who revoked the key",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UserResponse revokedBy;

    @Schema(
        description = "When the key was revoked",
        example = "2024-12-01T14:22:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime revokedAt;

    @Schema(
        description = "Total requests at the time of revocation",
        example = "1500",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long totalRequestsAtRevocation;
}
