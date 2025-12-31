package io.github.eventify.api.organization.service;

import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import static io.github.eventify.common.exception.ApiErrorCode.ORGANIZATION_NOT_FOUND_ERROR;

/**
 * Security service for organization access control.
 * Bean name "orgSecurity" for use in SpEL expressions.
 */
@Service("orgSecurity")
@RequiredArgsConstructor
public class OrganizationSecurityService {

    private final OrganizationMembershipRepository membershipRepository;

    private final OrganizationRepository organizationRepository;

    /**
     * Check if user is a member of the organization.
     *
     * @param orgId  the organization ID
     * @param userId the user ID
     * @return true if user is a member, false otherwise
     * @throws DataNotFoundException if organization does not exist
     */
    public boolean isMember(final Long orgId, final Long userId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new DataNotFoundException(ORGANIZATION_NOT_FOUND_ERROR);
        }
        return membershipRepository.existsByOrganizationIdAndUserId(orgId, userId);
    }

    /**
     * Check if user is owner or admin of the organization.
     *
     * @param orgId  the organization ID
     * @param userId the user ID
     * @return true if user is owner or admin, false otherwise
     * @throws DataNotFoundException if organization does not exist
     */
    public boolean isOwnerOrAdmin(final Long orgId, final Long userId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new DataNotFoundException(ORGANIZATION_NOT_FOUND_ERROR);
        }
        return membershipRepository.findByOrganizationIdAndUserId(orgId, userId)
            .map(OrganizationMembership::getRole)
            .map(role -> role == OrganizationalRole.OWNER || role == OrganizationalRole.ADMIN)
            .orElse(false);
    }

    /**
     * Check if user is the owner of the organization.
     *
     * @param orgId  the organization ID
     * @param userId the user ID
     * @return true if user is owner, false otherwise
     * @throws DataNotFoundException if organization does not exist
     */
    public boolean isOwner(final Long orgId, final Long userId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new DataNotFoundException(ORGANIZATION_NOT_FOUND_ERROR);
        }
        return membershipRepository.findByOrganizationIdAndUserId(orgId, userId)
            .map(OrganizationMembership::getRole)
            .map(role -> role == OrganizationalRole.OWNER)
            .orElse(false);
    }

    /**
     * Check if user can manage members (owner or admin).
     *
     * @param orgId  the organization ID
     * @param userId the user ID
     * @return true if user can manage members, false otherwise
     * @throws DataNotFoundException if organization does not exist
     */
    public boolean canManageMembers(final Long orgId, final Long userId) {
        return isOwnerOrAdmin(orgId, userId);
    }
}
