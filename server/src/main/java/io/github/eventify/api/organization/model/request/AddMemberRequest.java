package io.github.eventify.api.organization.model.request;

import io.github.eventify.api.organization.model.OrganizationalRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Request for adding a member to an organization.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class AddMemberRequest {

    @Schema(
        description = "Email address of the user to add as a member",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
        description = "Organizational role to assign to the member",
        example = "MEMBER",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OrganizationalRole role;
}
