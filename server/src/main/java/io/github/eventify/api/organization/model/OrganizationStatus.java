package io.github.eventify.api.organization.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing the status of an organization.
 */
@Getter
@AllArgsConstructor
public enum OrganizationStatus {

    TRIAL("Organization is in trial period"),
    ACTIVE("Organization has an active subscription"),
    SUSPENDED("Organization has been suspended");

    private final String description;
}
