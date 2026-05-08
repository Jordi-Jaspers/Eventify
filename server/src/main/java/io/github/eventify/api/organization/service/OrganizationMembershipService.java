package io.github.eventify.api.organization.service;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.OrganizationMembershipMetaData;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.AddMemberRequest;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.exception.DisabledUserException;
import io.github.eventify.common.exception.OwnerRoleException;
import io.github.eventify.common.exception.OwnershipTransferException;
import io.github.eventify.common.exception.UserAlreadyMemberException;
import io.github.jframe.datasource.search.JpaSearchSpecification;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.BadRequestException;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.authentication.model.Permission.MANAGE_ORGANIZATIONS;
import static io.github.eventify.api.user.model.UserMetaData.ORGANIZATION_TERM;
import static io.github.eventify.common.exception.ApiErrorCode.*;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;
import static java.util.Objects.nonNull;

/**
 * Service for managing organization memberships.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings(
    {
        "ClassDataAbstractionCoupling",
        "PMD.ExcessiveImports"
    }
)
public class OrganizationMembershipService {

    private static final int MAX_SEARCH_RESULTS = 10;

    private final UserService userService;

    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final OrganizationMembershipMetaData membershipMetaData;

    /**
     * Add a member to an organization.
     *
     * @param orgId   the organization ID
     * @param request the add member request
     * @return the created membership entity
     */
    @Transactional
    public OrganizationMembership addMember(final Long orgId, final AddMemberRequest request) {
        final User user = userService.findByEmail(request.getEmail());
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
     * Assign an owner to an organization (admin only).
     *
     * @param orgId  the organization ID
     * @param email  the email of the user to assign as owner (optional)
     * @param userId the user ID to assign as owner (optional)
     * @return the created or updated membership entity
     */
    @Transactional
    public OrganizationMembership assignOwner(final Long orgId, final String email, final Long userId) {
        if (membershipRepository.existsByOrganizationIdAndRole(orgId, OrganizationalRole.OWNER)) {
            throw new OwnerRoleException(ORGANIZATION_ALREADY_HAS_OWNER_ERROR);
        }

        final Organization organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new DataNotFoundException(ORGANIZATION_NOT_FOUND_ERROR));

        final User user = nonNull(email)
            ? userService.findByEmail(email)
            : userService.findById(userId);

        if (!user.isEnabled()) {
            throw new DisabledUserException();
        }

        final OrganizationMembership existingMembership = membershipRepository
            .findByOrganizationIdAndUserId(orgId, user.getId())
            .orElse(null);

        if (nonNull(existingMembership)) {
            existingMembership.setRole(OrganizationalRole.OWNER);
            return membershipRepository.save(existingMembership);
        } else {
            final OrganizationMembership membership = new OrganizationMembership(organization, user, OrganizationalRole.OWNER);
            membership.setInvitedBy(getLoggedInUser());
            return membershipRepository.save(membership);
        }
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

        canUpdateMemberRole(orgId, membership, getLoggedInUser());

        membership.setRole(role);
        final OrganizationMembership saved = membershipRepository.save(membership);
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

        canUpdateMemberRole(orgId, membership, callerUser);
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

        final User caller = getLoggedInUser();
        if (!caller.hasPermission(MANAGE_ORGANIZATIONS) && !caller.getId().equals(currentOwnerId)) {
            throw new OwnershipTransferException(NOT_ORGANIZATION_OWNER_ERROR);
        }

        final OrganizationMembership currentOwnerMembership = membershipRepository
            .findByOrganizationIdAndUserId(orgId, currentOwnerId)
            .orElseThrow(() -> new OwnershipTransferException(NOT_ORGANIZATION_OWNER_ERROR));

        if (currentOwnerMembership.getRole() != OrganizationalRole.OWNER) {
            throw new OwnershipTransferException(NOT_ORGANIZATION_OWNER_ERROR);
        }

        final OrganizationMembership newOwnerMembership = membershipRepository
            .findByOrganizationIdAndUserId(orgId, newOwnerId)
            .orElseThrow(() -> new BadRequestException(NOT_MEMBER_OF_ORGANIZATION_ERROR.getReason()));

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
        if (!organizationRepository.existsById(orgId)) {
            throw new DataNotFoundException(ORGANIZATION_NOT_FOUND_ERROR);
        }
        return membershipRepository.findAllByOrganizationIdWithUser(orgId);
    }

    /**
     * Get all organizations a user is a member of.
     *
     * @return list of membership entities with organization data
     */
    @Transactional(readOnly = true)
    public List<OrganizationMembership> getUserOrganizations() {
        return membershipRepository.findAllByUserIdWithOrganization(getLoggedInUser().getId());
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
        searchInput.setTextValue('!' + orgId.toString());
        input.addSearchInput(searchInput);

        return userService.searchUsers(input);
    }

    /**
     * Search organization members with pagination, filtering, and sorting.
     *
     * @param input the sortable page input containing search parameters
     * @param orgId the organization ID
     * @return page of organization memberships matching the search criteria
     */
    @Transactional(readOnly = true)
    public Page<OrganizationMembership> searchOrganizationMembers(final SortablePageInput input, final Long orgId) {
        final SearchInput searchInput = new SearchInput();
        searchInput.setFieldName(ORGANIZATION_TERM);
        searchInput.setTextValue(orgId.toString());
        input.addSearchInput(searchInput);

        final Sort sort = membershipMetaData.toSort(input.getSortOrder());
        final Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);

        final List<SearchCriterium> criteria = membershipMetaData.toSearchCriteria(input.getSearchInputs());
        final Specification<OrganizationMembership> spec = new JpaSearchSpecification<>(criteria);
        return membershipRepository.findAll(spec, pageable);
    }

    private void canUpdateMemberRole(final Long orgId, final OrganizationMembership membership, final User caller) {
        final boolean isGlobalAdmin = caller.hasPermission(MANAGE_ORGANIZATIONS);
        final OrganizationMembership callerMembership = membershipRepository
            .findByOrganizationIdAndUserId(orgId, caller.getId())
            .orElse(null);

        if (!isGlobalAdmin && callerMembership == null) {
            throw new AccessDeniedException(NOT_MEMBER_OF_ORGANIZATION_ERROR.getReason());
        }

        if (!isGlobalAdmin && callerMembership.getRole() == OrganizationalRole.ADMIN && membership.getRole() != OrganizationalRole.MEMBER) {
            throw new AccessDeniedException(CANNOT_UPDATE_ROLE_AS_ADMIN_ERROR.getReason());
        }

        if (membership.getRole() == OrganizationalRole.OWNER) {
            throw new OwnerRoleException(CANNOT_CHANGE_OWNER_ROLE_ERROR);
        }
    }
}
