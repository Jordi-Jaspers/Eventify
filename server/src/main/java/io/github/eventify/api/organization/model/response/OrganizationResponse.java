package io.github.eventify.api.organization.model.response;

import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.user.model.response.UserResponse;
import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response containing organization details.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OrganizationResponse implements PageableItemResource {

    @Schema(
        description = "Unique organization identifier",
        example = "12345",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long id;

    @Schema(
        description = "Organization name",
        example = "Acme Corporation",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String name;

    @Schema(
        description = "URL-friendly organization slug",
        example = "acme-corporation",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String slug;

    @Schema(
        description = "Current status of the organization",
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OrganizationStatus status;

    @Schema(
        description = "User ID of the organization creator",
        example = "12345",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long createdBy;

    @Schema(
        description = "Timestamp when the organization was created",
        example = "2026-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime createdAt;

    @Schema(
        description = "Details of the organization owner",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private UserResponse owner;

    @Schema(
        description = "Number of members in the organization",
        example = "42",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private int memberCount;
}
