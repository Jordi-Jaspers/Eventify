package io.github.eventify.api.organization.repository;

import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.OrganizationalRole;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link OrganizationMembership} entities.
 */
@Repository
public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, Long> {

    /**
     * Find a membership by organization and user ID.
     *
     * @param orgId  the organization ID
     * @param userId the user ID
     * @return Optional containing membership if found
     */
    Optional<OrganizationMembership> findByOrganizationIdAndUserId(Long orgId, Long userId);

    /**
     * Check if a membership exists for organization and user.
     *
     * @param orgId  the organization ID
     * @param userId the user ID
     * @return true if exists, false otherwise
     */
    boolean existsByOrganizationIdAndUserId(Long orgId, Long userId);

    /**
     * Find all memberships for an organization with user data eagerly loaded.
     *
     * @param orgId the organization ID
     * @return list of memberships with user data
     */
    @Query("SELECT m FROM OrganizationMembership m JOIN FETCH m.user JOIN FETCH m.organization WHERE m.organization.id = :orgId")
    List<OrganizationMembership> findAllByOrganizationIdWithUser(@Param("orgId") Long orgId);

    /**
     * Find all memberships for an organization.
     *
     * @param orgId the organization ID
     * @return list of memberships
     */
    List<OrganizationMembership> findAllByOrganizationId(Long orgId);

    /**
     * Find all memberships for a user with organization data eagerly loaded.
     *
     * @param userId the user ID
     * @return list of memberships with organization data
     */
    @Query("SELECT m FROM OrganizationMembership m JOIN FETCH m.organization JOIN FETCH m.user WHERE m.user.id = :userId")
    List<OrganizationMembership> findAllByUserIdWithOrganization(@Param("userId") Long userId);

    /**
     * Find all memberships for a user.
     *
     * @param userId the user ID
     * @return list of memberships
     */
    List<OrganizationMembership> findAllByUserId(Long userId);

    /**
     * Find a membership by organization and role.
     *
     * @param orgId the organization ID
     * @param role  the organizational role
     * @return Optional containing membership if found
     */
    Optional<OrganizationMembership> findByOrganizationIdAndRole(Long orgId, OrganizationalRole role);

    /**
     * Check if a membership exists for organization and role.
     *
     * @param orgId the organization ID
     * @param role  the organizational role
     * @return true if exists, false otherwise
     */
    boolean existsByOrganizationIdAndRole(Long orgId, OrganizationalRole role);
}
