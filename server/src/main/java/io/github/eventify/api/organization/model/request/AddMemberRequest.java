package io.github.eventify.api.organization.model.request;

import io.github.eventify.api.organization.model.OrganizationalRole;
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

    private String email;

    private OrganizationalRole role;
}
