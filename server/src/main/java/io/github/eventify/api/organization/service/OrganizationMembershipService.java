package io.github.eventify.api.organization.service;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.AddMemberRequest;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.exception.DisabledUserException;
import io.github.eventify.common.exception.OwnerRoleException;
import io.github.eventify.common.exception.OwnershipTransferException;
import io.github.eventify.common.exception.UserAlreadyMemberException;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.BadRequestException;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.user.model.UserMetaData.ORGANIZATION_TERM;
import static io.github.eventify.common.exception.ApiErrorCode.*;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;

/**
 * Service for managing organization memberships.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("ClassDataAbstractionCoupling")
public class OrganizationMembershipService {

    private static final int MAX_SEARCH_RESULTS = 10;
    private static final String NOT_A_MEMBER_MESSAGE = "Not a member of this organization";

    private final UserService userService;
    private final OrganizationMembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    /**
     * Add a member to an organization.
     *
     * @param orgId   the organization ID
     * @param request the add member request
     * @return the created membership entity
     */
    @Transactional
    public OrganizationMembership addMember(final Long orgId, final AddMemberRequest request) {
        final User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND_ERROR));

        final Organization organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new DataNotFoundException(ORGANIZATION_NOT_FOUND_ERROR));

        if (!user.isEnabled()) {
            throw new DisabledUserException();
        }

        if (membershipRepository.existsByOrganizationIdAndUserId(orgId, user.getId())) {
            throw new UserAlreadyMemberException();
        }

        final OrganizationMembership membership = new OrganizationMembership(organization, user, request.getRole());
        membership.setInvitedBy(getLoggedInUser());

        return membershipRepository.save(membership);
    }

    /**
     * Update a member's role in an organization.
     *
     * @param orgId  the organization ID
     * @param userId the user ID
     * @param role   the new role
     * @return the updated membership entity
     */
    @Transactional
    public OrganizationMembership updateMemberRole(final Long orgId, final Long userId, final OrganizationalRole role) {
        if (role == OrganizationalRole.OWNER) {
            throw new OwnerRoleException(CANNOT_SET_OWNER_ROLE_ERROR);
        }

        final OrganizationMembership membership = membershipRepository.findByOrganizationIdAndUserId(orgId, userId)
            .orElseThrow(() -> new DataNotFoundException(MEMBERSHIP_NOT_FOUND_ERROR));

        // Get caller's membership to check permissions
        final OrganizationMembership callerMembership = membershipRepository
            .findByOrganizationIdAndUserId(orgId, getLoggedInUser().getId())
            .orElseThrow(() -> new AccessDeniedException(NOT_A_MEMBER_MESSAGE));

        // Admins can only update MEMBERs (not OWNER or other ADMIN)
        if (callerMembership.getRole() == OrganizationalRole.ADMIN
            && membership.getRole() != OrganizationalRole.MEMBER) {
            throw new AccessDeniedException("Admins can only update MEMBER roles");
        }

        // Owner role cannot be changed (must use transfer ownership)
        if (membership.getRole() == OrganizationalRole.OWNER) {
            throw new OwnerRoleException(CANNOT_CHANGE_OWNER_ROLE_ERROR);
        }

        membership.setRole(role);
        final OrganizationMembership saved = membershipRepository.save(membership);

        // Return membership with eagerly loaded associations for mapping
        return membershipRepository.findAllByOrganizationIdWithUser(orgId).stream()
            .filter(m -> m.getUser().getId().equals(userId))
            .findFirst()
            .orElse(saved);
    }

    /**
     * Remove a member from an organization.
     *
     * @param orgId      the organization ID
     * @param userId     the user ID
     * @param callerUser the user making the request
     */
    @Transactional
    public void removeMember(final Long orgId, final Long userId, final User callerUser) {
        final OrganizationMembership membership = membershipRepository.findByOrganizationIdAndUserId(orgId, userId)
            .orElseThrow(() -> new DataNotFoundException(MEMBERSHIP_NOT_FOUND_ERROR));

        // Get caller's membership to check permissions
        final OrganizationMembership callerMembership = membershipRepository
            .findByOrganizationIdAndUserId(orgId, callerUser.getId())
            .orElseThrow(() -> new AccessDeniedException(NOT_A_MEMBER_MESSAGE));

        // Admins can only remove MEMBERs (not OWNER or other ADMIN)
        if (callerMembership.getRole() == OrganizationalRole.ADMIN
            && membership.getRole() != OrganizationalRole.MEMBER) {
            throw new AccessDeniedException("Admins can only remove MEMBER roles");
        }

        // Owner cannot be removed
        if (membership.getRole() == OrganizationalRole.OWNER) {
            throw new OwnerRoleException(CANNOT_REMOVE_OWNER_ERROR);
        }

        membershipRepository.delete(membership);
    }

    /**
     * Transfer ownership of an organization.
     *
     * @param orgId          the organization ID
     * @param currentOwnerId the current owner user ID
     * @param newOwnerId     the new owner user ID
     */
    @Transactional
    public void transferOwnership(final Long orgId, final Long currentOwnerId, final Long newOwnerId) {
        if (currentOwnerId.equals(newOwnerId)) {
            throw new OwnershipTransferException(CANNOT_TRANSFER_TO_SELF_ERROR);
        }

        final OrganizationMembership currentOwnerMembership = membershipRepository
            .findByOrganizationIdAndUserId(orgId, currentOwnerId)
            .orElseThrow(() -> new DataNotFoundException(MEMBERSHIP_NOT_FOUND_ERROR));

        if (currentOwnerMembership.getRole() != OrganizationalRole.OWNER) {
            throw new OwnershipTransferException(NOT_ORGANIZATION_OWNER_ERROR);
        }

        final OrganizationMembership newOwnerMembership = membershipRepository
            .findByOrganizationIdAndUserId(orgId, newOwnerId)
            .orElseThrow(() -> new BadRequestException("Target user is not a member of this organization"));

        // Transfer ownership
        currentOwnerMembership.setRole(OrganizationalRole.ADMIN);
        newOwnerMembership.setRole(OrganizationalRole.OWNER);

        membershipRepository.saveAll(List.of(currentOwnerMembership, newOwnerMembership));
    }

    /**
     * Get all members of an organization.
     *
     * @param orgId the organization ID
     * @return list of membership entities with user data
     * @throws DataNotFoundException if the organization does not exist
     */
    @Transactional(readOnly = true)
    public List<OrganizationMembership> getOrganizationMembers(final Long orgId) {
        // Verify organization exists first
        if (!organizationRepository.existsById(orgId)) {
            throw new DataNotFoundException(ORGANIZATION_NOT_FOUND_ERROR);
        }
        return membershipRepository.findAllByOrganizationIdWithUser(orgId);
    }

    /**
     * Get all organizations a user is a member of.
     *
     * @param userId the user ID
     * @return list of membership entities with organization data
     */
    @Transactional(readOnly = true)
    public List<OrganizationMembership> getUserOrganizations(final Long userId) {
        return membershipRepository.findAllByUserIdWithOrganization(userId);
    }

    /**
     * Search for users to add to an organization.
     *
     * @param input the sortable page input containing search parameters
     * @param orgId the organization ID
     * @return page of users matching the search criteria
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsersForOrganization(final SortablePageInput input, final Long orgId) {
        input.setPageSize(MAX_SEARCH_RESULTS);

        final SearchInput searchInput = new SearchInput();
        searchInput.setFieldName(ORGANIZATION_TERM);
        searchInput.setTextValue(orgId.toString());
        input.addSearchInput(searchInput);

        return userService.searchUsers(input);
    }
}
