package io.github.eventify.api.organization.controller;

import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.mapper.OrganizationMembershipMapper;
import io.github.eventify.api.organization.model.request.AddMemberRequest;
import io.github.eventify.api.organization.model.request.TransferOwnershipRequest;
import io.github.eventify.api.organization.model.request.UpdateMemberRoleRequest;
import io.github.eventify.api.organization.model.response.OrganizationMembershipResponse;
import io.github.eventify.api.organization.model.response.UserOrganizationResponse;
import io.github.eventify.api.organization.model.validator.AddMemberRequestValidator;
import io.github.eventify.api.organization.model.validator.TransferOwnershipRequestValidator;
import io.github.eventify.api.organization.model.validator.UpdateMemberRoleRequestValidator;
import io.github.eventify.api.organization.service.OrganizationMembershipService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.mapper.UserMapper;
import io.github.eventify.api.user.model.response.UserResponse;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for organization membership management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Organization Membership",
    description = "Endpoints for managing organization memberships"
)
@SuppressWarnings(
    {
        "ClassFanOutComplexity",
        "PMD.ExcessiveImports",
        "PMD.CouplingBetweenObjects"
    }
)
public class OrganizationMembershipController {

    private final OrganizationMembershipService membershipService;
    private final OrganizationMembershipMapper membershipMapper;
    private final AddMemberRequestValidator addMemberValidator;
    private final UpdateMemberRoleRequestValidator updateRoleValidator;
    private final TransferOwnershipRequestValidator transferOwnershipValidator;
    private final UserMapper userMapper;

    /**
     * Search for users to add to an organization.
     *
     * @param orgId     the organization ID
     * @param principal the authenticated user
     * @return list of matching users
     */
    @Operation(summary = "Search users to add to organization")
    @GetMapping(
        path = ORGANIZATION_MEMBERS_SEARCH_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')")
    public ResponseEntity<PageResource<UserResponse>> searchUsers(@PathVariable final Long orgId,
        @RequestBody final SortablePageInput input,
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        final Page<User> page = membershipService.searchUsersForOrganization(input, orgId);
        return ResponseEntity.status(OK).body(userMapper.toPageResource(page));
    }

    /**
     * Add a member to an organization.
     *
     * @param orgId     the organization ID
     * @param request   the add member request
     * @param principal the authenticated user
     * @return the created membership response
     */
    @Operation(summary = "Add a member to an organization")
    @PostMapping(
        path = ORGANIZATION_MEMBERS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')")
    public ResponseEntity<OrganizationMembershipResponse> addMember(
        @PathVariable final Long orgId,
        @RequestBody final AddMemberRequest request,
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        addMemberValidator.validateAndThrow(request);
        final OrganizationMembership membership = membershipService.addMember(orgId, request);
        final OrganizationMembershipResponse response = membershipMapper.toMembershipResponse(membership);
        return ResponseEntity.status(OK).body(response);
    }

    /**
     * Get all members of an organization.
     *
     * @param orgId     the organization ID
     * @param principal the authenticated user
     * @return list of organization members
     */
    @Operation(summary = "Get all members of an organization")
    @GetMapping(
        path = ORGANIZATION_MEMBERS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@orgSecurity.isMember(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')")
    public ResponseEntity<List<OrganizationMembershipResponse>> getOrganizationMembers(@PathVariable final Long orgId,
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        final List<OrganizationMembership> memberships = membershipService.getOrganizationMembers(orgId);
        final List<OrganizationMembershipResponse> responses = memberships.stream()
            .map(membershipMapper::toMembershipResponse)
            .toList();
        return ResponseEntity.status(OK).body(responses);
    }

    /**
     * Update a member's role.
     *
     * @param orgId     the organization ID
     * @param userId    the user ID
     * @param request   the update role request
     * @param principal the authenticated user
     * @return the updated membership response
     */
    @Operation(summary = "Update a member's role")
    @PatchMapping(
        path = ORGANIZATION_MEMBER_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')")
    public ResponseEntity<OrganizationMembershipResponse> updateMemberRole(
        @PathVariable final Long orgId,
        @PathVariable final Long userId,
        @RequestBody final UpdateMemberRoleRequest request,
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        updateRoleValidator.validateAndThrow(request);
        final OrganizationMembership membership = membershipService.updateMemberRole(orgId, userId, request.getRole());
        final OrganizationMembershipResponse response = membershipMapper.toMembershipResponse(membership);
        return ResponseEntity.status(OK).body(response);
    }

    /**
     * Remove a member from an organization.
     *
     * @param orgId     the organization ID
     * @param userId    the user ID
     * @param principal the authenticated user
     */
    @Operation(summary = "Remove a member from an organization")
    @DeleteMapping(path = ORGANIZATION_MEMBER_PATH)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')")
    public ResponseEntity<Void> removeMember(@PathVariable final Long orgId, @PathVariable final Long userId,
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        membershipService.removeMember(orgId, userId, principal.getUser());
        return ResponseEntity.status(NO_CONTENT).build();
    }

    /**
     * Transfer ownership of an organization.
     *
     * @param orgId     the organization ID
     * @param request   the transfer ownership request
     * @param principal the authenticated user
     */
    @Operation(summary = "Transfer ownership of an organization")
    @PostMapping(
        path = ORGANIZATION_TRANSFER_OWNERSHIP_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@orgSecurity.isOwner(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')")
    public ResponseEntity<Void> transferOwnership(@PathVariable final Long orgId, @RequestBody final TransferOwnershipRequest request,
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        transferOwnershipValidator.validateAndThrow(request);
        membershipService.transferOwnership(orgId, request.getCurrentOwnerUserId(), request.getNewOwnerUserId());
        return ResponseEntity.status(OK).build();
    }

    /**
     * Get all organizations the authenticated user is a member of.
     *
     * @return list of user organizations
     */
    @Operation(summary = "Get all organizations for the authenticated user")
    @GetMapping(
        path = USER_ORGANIZATIONS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<UserOrganizationResponse>> getUserOrganizations() {
        final List<OrganizationMembership> memberships = membershipService.getUserOrganizations();
        final List<UserOrganizationResponse> responses = memberships.stream()
            .map(membershipMapper::toUserOrganizationResponse)
            .toList();
        return ResponseEntity.status(OK).body(responses);
    }
}
