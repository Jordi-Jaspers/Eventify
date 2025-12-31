package io.github.eventify.api.organization.model.response;

import io.github.eventify.api.organization.model.OrganizationalRole;
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

    private Long organizationId;

    private String organizationName;

    private String organizationSlug;

    private OrganizationalRole role;

    private OffsetDateTime joinedAt;
}
