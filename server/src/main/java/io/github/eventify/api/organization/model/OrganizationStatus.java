package io.github.eventify.api.organization.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing the status of an organization.
 */
@Getter
@AllArgsConstructor
@Schema(description = "OrganizationStatus")
public enum OrganizationStatus {

    ACTIVE("Organization has an active subscription"),
    SUSPENDED("Organization has been suspended");

    private final String description;
}
