package io.github.eventify.api.organization.model.request;

import io.github.eventify.api.organization.model.OrganizationalRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Request for updating a member's role in an organization.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateMemberRoleRequest {

    @Schema(
        description = "New organizational role to assign to the member",
        example = "ADMIN",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OrganizationalRole role;
}
