package io.github.eventify.api.organization.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Request to provision a new organization.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ProvisionOrganizationRequest {

    private String name;
}
