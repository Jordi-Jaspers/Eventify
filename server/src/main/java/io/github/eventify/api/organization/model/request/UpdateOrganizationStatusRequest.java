package io.github.eventify.api.organization.model.request;

import io.github.eventify.api.organization.model.OrganizationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Request DTO for updating an organization's status.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateOrganizationStatusRequest {

    private OrganizationStatus status;

}
