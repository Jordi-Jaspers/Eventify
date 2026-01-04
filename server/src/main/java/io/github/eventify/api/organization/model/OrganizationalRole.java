package io.github.eventify.api.organization.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing organizational roles.
 */
@Getter
@AllArgsConstructor
@Schema(description = "OrganizationalRole")
public enum OrganizationalRole {
    OWNER,
    ADMIN,
    MEMBER
}
