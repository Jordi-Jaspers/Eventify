package io.github.eventify.api.organization.controller;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.service.OrganizationService;
import io.github.eventify.api.user.model.request.UpdateRetentionRequest;
import io.github.eventify.api.user.model.response.RetentionSettingsResponse;
import io.github.eventify.api.user.model.validator.RetentionValidator;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.jframe.validation.ValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ORGANIZATION_RETENTION_SETTINGS_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for organization retention settings endpoints.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Organization Retention Settings",
    description = "Endpoints for managing organization data retention settings (Owner/Admin only)"
)
public class OrganizationSettingsController {

    private final OrganizationService organizationService;
    private final RetentionValidator retentionValidator;

    @ResponseStatus(OK)
    @Operation(summary = "Get retention settings for organization (Owner/Admin only)")
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id)")
    @GetMapping(
        path = ORGANIZATION_RETENTION_SETTINGS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RetentionSettingsResponse> getRetentionSettings(
        @PathVariable final Long orgId,
        @AuthenticationPrincipal final UserTokenPrincipal principal
    ) {
        final Organization organization = organizationService.findOrganizationById(orgId);
        final RetentionSettingsResponse response = new RetentionSettingsResponse();
        response.setRetentionDays(organization.getRetentionDays());
        return ResponseEntity.status(OK).body(response);
    }

    @ResponseStatus(OK)
    @Operation(summary = "Update retention settings for organization (Owner/Admin only)")
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id)")
    @PutMapping(
        path = ORGANIZATION_RETENTION_SETTINGS_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RetentionSettingsResponse> updateRetentionSettings(
        @PathVariable final Long orgId,
        @RequestBody final UpdateRetentionRequest request,
        @AuthenticationPrincipal final UserTokenPrincipal principal
    ) {
        retentionValidator.validate(request, new ValidationResult());
        final Organization organization = organizationService.findOrganizationById(orgId);
        final Organization updatedOrg = organizationService.updateRetentionDays(organization, request.getRetentionDays());
        final RetentionSettingsResponse response = new RetentionSettingsResponse();
        response.setRetentionDays(updatedOrg.getRetentionDays());
        return ResponseEntity.status(OK).body(response);
    }
}
