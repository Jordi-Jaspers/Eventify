package io.github.eventify.api.organization.model.response;

import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response containing organization membership details.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class OrganizationMembershipResponse implements PageableItemResource {

    @Schema(
        description = "Unique membership identifier",
        example = "12345",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long id;

    @Schema(
        description = "Organization identifier",
        example = "12345",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long organizationId;

    @Schema(
        description = "User identifier",
        example = "12345",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long userId;

    @Schema(
        description = "User's email address",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String userEmail;

    @Schema(
        description = "User's first name",
        example = "John",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String userFirstName;

    @Schema(
        description = "User's last name",
        example = "Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String userLastName;

    @Schema(
        description = "User's role within the organization",
        example = "MEMBER",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OrganizationalRole role;

    @Schema(
        description = "Timestamp when the user joined the organization",
        example = "2026-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime joinedAt;
}
