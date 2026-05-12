package io.github.eventify.api.organization.repository;

import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.OrganizationalRole;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for {@link OrganizationMembership} entities.
 */
@Repository
public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, Long>,
                                                  JpaSpecificationExecutor<OrganizationMembership> {

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

    /**
     * Override findAll to eagerly fetch user and organization.
     *
     * @param spec     the specification
     * @param pageable the pageable
     * @return page of memberships with user and organization eagerly loaded
     */
    @EntityGraph(
        attributePaths = {
            "user",
            "organization"
        }
    )
    @NonNull
    @Override
    Page<OrganizationMembership> findAll(@NonNull Specification<OrganizationMembership> spec, @NonNull Pageable pageable);

    /**
     * Find all distinct users who are OWNER in any organization.
     *
     * @return list of distinct owner users
     */
    @Query("SELECT DISTINCT m.user FROM OrganizationMembership m WHERE m.role = 'OWNER'")
    List<io.github.eventify.api.user.model.User> findAllOwnersDistinct();

    /**
     * Count memberships for a given organization.
     *
     * @param organizationId the organization ID
     * @return count of memberships
     */
    long countByOrganizationId(Long organizationId);

    /**
     * Count distinct users who are OWNER in any organization.
     *
     * @return count of distinct owners
     */
    @Query("SELECT COUNT(DISTINCT m.user) FROM OrganizationMembership m WHERE m.role = 'OWNER'")
    long countDistinctOwners();

    /**
     * Delete all memberships for users with the given IDs.
     *
     * @param userIds the user IDs
     */
    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM OrganizationMembership m WHERE m.user.id IN :userIds")
    void deleteAllByUserIdIn(@Param("userIds") List<Long> userIds);

    /**
     * Delete all memberships for organizations with names containing the given pattern.
     *
     * @param namePattern the name pattern
     */
    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM OrganizationMembership m WHERE m.organization.name LIKE %:namePattern%")
    void deleteAllByOrganizationNameContaining(@Param("namePattern") String namePattern);

    /**
     * Delete all memberships for organizations created by users with the given IDs.
     *
     * @param creatorUserIds the creator user IDs
     */
    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM OrganizationMembership m WHERE m.organization.createdBy IN :creatorUserIds")
    void deleteAllByOrganizationCreatedByIn(@Param("creatorUserIds") List<Long> creatorUserIds);
}
