package io.github.eventify.api.notification.adapter.repository;

import io.github.eventify.api.notification.adapter.model.AdapterConfig;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link AdapterConfig} entities.
 */
@Repository
public interface AdapterConfigRepository extends JpaRepository<AdapterConfig, Long> {

    /**
     * Find personal adapter configs for a user (no organization).
     *
     * @param userId the user ID
     * @return list of personal configs
     */
    List<AdapterConfig> findByUserIdAndOrganizationIdIsNull(Long userId);

    /**
     * Find all adapter configs for an organization.
     *
     * @param organizationId the organization ID
     * @return list of org configs
     */
    List<AdapterConfig> findByOrganizationId(Long organizationId);

    /**
     * Find a personal config by ID and owner user ID.
     *
     * @param id     the config ID
     * @param userId the user ID
     * @return Optional containing the config if found and owned by user
     */
    Optional<AdapterConfig> findByIdAndUserId(Long id, Long userId);
}
