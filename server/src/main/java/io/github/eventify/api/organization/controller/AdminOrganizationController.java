package io.github.eventify.api.organization.controller;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.mapper.OrganizationMapper;
import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.eventify.api.organization.model.response.OrganizationResponse;
import io.github.eventify.api.organization.model.validator.OrganizationValidator;
import io.github.eventify.api.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ADMIN_ORGANIZATIONS_PATH;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for admin organization management endpoints.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Admin Organization Management",
    description = "Endpoints for global administrators to provision and manage organizations"
)
public class AdminOrganizationController {

    private final OrganizationService organizationService;

    private final OrganizationMapper organizationMapper;

    private final OrganizationValidator validator;

    @ResponseStatus(CREATED)
    @PreAuthorize("hasAnyAuthority('PROVISION_ORGANIZATIONS')")
    @Operation(summary = "Provision a new organization (Admin only)")
    @PostMapping(
        path = ADMIN_ORGANIZATIONS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OrganizationResponse> provisionOrganization(@RequestBody final ProvisionOrganizationRequest request) {
        validator.validateAndThrow(request);
        final Organization organization = organizationService.create(request);
        final OrganizationResponse response = organizationMapper.toOrganizationResponse(organization);
        return ResponseEntity.status(CREATED).body(response);
    }
}
