package io.github.eventify.api.organization.model.response;

import io.github.eventify.api.organization.model.OrganizationalRole;
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
public class OrganizationMembershipResponse {

    private Long id;

    private Long organizationId;

    private Long userId;

    private String userEmail;

    private String userFirstName;

    private String userLastName;

    private OrganizationalRole role;

    private OffsetDateTime joinedAt;
}
