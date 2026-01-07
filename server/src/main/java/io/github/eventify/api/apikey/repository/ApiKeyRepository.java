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
     * Find an API key by its prefix.
     *
     * @param prefix the prefix to search for
     * @return Optional containing the API key if found
     */
    Optional<ApiKey> findByPrefix(String prefix);

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
     * Check if an API key exists with the given prefix and hashed key.
     *
     * @param prefix    the key prefix
     * @param hashedKey the hashed key
     * @return true if exists, false otherwise
     */
    boolean existsByPrefixAndHashedKey(String prefix, String hashedKey);
}
