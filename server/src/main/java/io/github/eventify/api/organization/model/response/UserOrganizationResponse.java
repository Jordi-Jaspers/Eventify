package io.github.eventify.api.organization.model.response;

import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response containing user's organization membership details.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserOrganizationResponse {

    @Schema(
        description = "Unique organization identifier",
        example = "12345",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long organizationId;

    @Schema(
        description = "Organization name",
        example = "Acme Corporation",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String organizationName;

    @Schema(
        description = "URL-friendly organization slug",
        example = "acme-corporation",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String organizationSlug;

    @Schema(
        description = "Organization status",
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OrganizationStatus organizationStatus;

    @Schema(
        description = "User's role within the organization",
        example = "MEMBER",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OrganizationalRole role;

    @Schema(
        description = "Timestamp when the user joined the organization",
        example = "2026-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime joinedAt;
}
