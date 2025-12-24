package io.github.eventify.api.organization.repository;

import io.github.eventify.api.organization.model.OrganizationMembership;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link OrganizationMembership} entities.
 */
@Repository
public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, Long> {
}
