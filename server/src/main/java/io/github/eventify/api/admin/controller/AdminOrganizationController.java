package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.request.AssignOwnerRequest;
import io.github.eventify.api.admin.model.validator.AssignOwnerRequestValidator;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.mapper.OrganizationMapper;
import io.github.eventify.api.organization.model.mapper.OrganizationMembershipMapper;
import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.eventify.api.organization.model.response.OrganizationMembershipResponse;
import io.github.eventify.api.organization.model.response.OrganizationResponse;
import io.github.eventify.api.organization.model.validator.OrganizationValidator;
import io.github.eventify.api.organization.service.OrganizationMembershipService;
import io.github.eventify.api.organization.service.OrganizationService;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ADMIN_ORGANIZATIONS_PATH;
import static io.github.eventify.api.Paths.ADMIN_ORGANIZATIONS_SEARCH_PATH;
import static io.github.eventify.api.Paths.ADMIN_ORGANIZATION_ASSIGN_OWNER_PATH;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
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
@SuppressWarnings("PMD.ExcessiveImports")
public class AdminOrganizationController {

    private final OrganizationService organizationService;

    private final OrganizationMembershipService membershipService;

    private final OrganizationMapper organizationMapper;

    private final OrganizationMembershipMapper membershipMapper;

    private final OrganizationValidator validator;

    private final AssignOwnerRequestValidator assignOwnerValidator;

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
        final OrganizationResponse response = organizationMapper.toResourceObject(organization);
        return ResponseEntity.status(CREATED).body(response);
    }

    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(summary = "Search organizations with pagination and filtering")
    @PostMapping(
        path = ADMIN_ORGANIZATIONS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageResource<OrganizationResponse>> searchOrganizations(@RequestBody final SortablePageInput input) {
        final Page<Organization> page = organizationService.searchOrganizations(input);
        return ResponseEntity.status(OK).body(organizationMapper.toPageResource(page));
    }

    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(summary = "Assign an owner to an organization (Admin only)")
    @PostMapping(
        path = ADMIN_ORGANIZATION_ASSIGN_OWNER_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OrganizationMembershipResponse> assignOwner(
        @PathVariable final Long orgId,
        @RequestBody final AssignOwnerRequest request) {
        assignOwnerValidator.validateAndThrow(request);
        final OrganizationMembership membership = membershipService.assignOwner(orgId, request.getEmail(), request.getUserId());
        final OrganizationMembershipResponse response = membershipMapper.toMembershipResponse(membership);
        return ResponseEntity.status(OK).body(response);
    }
}
