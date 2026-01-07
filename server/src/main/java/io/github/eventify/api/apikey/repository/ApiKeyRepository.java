package io.github.eventify.api.apikey.repository;

import io.github.eventify.api.apikey.model.ApiKey;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link ApiKey} entities.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long>, JpaSpecificationExecutor<ApiKey> {

    /**
     * Find all API keys by user ID.
     *
     * @param userId the user ID
     * @return list of API keys
     */
    List<ApiKey> findAllByUserId(Long userId);

    /**
     * Find all API keys by organization ID.
     *
     * @param organizationId the organization ID
     * @return list of API keys
     */
    List<ApiKey> findAllByOrganizationId(Long organizationId);

    /**
     * Find all user personal API keys (organization ID is null) ordered by creation date descending.
     *
     * @param userId the user ID
     * @return list of personal API keys
     */
    List<ApiKey> findAllByUserIdAndOrganizationIdIsNullOrderByCreatedAtDesc(Long userId);

    /**
     * Find all user personal API keys (organization ID is null).
     *
     * @param userId the user ID
     * @return list of personal API keys
     */
    List<ApiKey> findAllByUserIdAndOrganizationIdIsNull(Long userId);

    /**
     * Count user personal API keys (organization ID is null).
     *
     * @param userId the user ID
     * @return count of personal API keys
     */
    Long countByUserIdAndOrganizationIdIsNull(Long userId);

    /**
     * Find an API key by ID, user ID, and ensure it's a personal key (organization ID is null).
     *
     * @param id     the API key ID
     * @param userId the user ID
     * @return Optional containing the API key if found
     */
    Optional<ApiKey> findByIdAndUserId(Long id, Long userId);
}
