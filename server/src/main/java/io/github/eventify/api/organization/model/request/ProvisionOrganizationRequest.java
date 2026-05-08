package io.github.eventify.api.organization.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(
        description = "Name of the organization",
        example = "Acme Corporation",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "Email address of the organization owner",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String owner;
}
