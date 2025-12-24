package io.github.eventify.api.organization.model.response;

import io.github.eventify.api.organization.model.OrganizationStatus;
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
public class OrganizationResponse {

    private Long id;

    private String name;

    private String slug;

    private OrganizationStatus status;

    private Long createdBy;

    private OffsetDateTime createdAt;

    private OwnerResponse owner;
}
