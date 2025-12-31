package io.github.eventify.api.organization.model.request;

import io.github.eventify.api.organization.model.OrganizationalRole;
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

    private OrganizationalRole role;
}
