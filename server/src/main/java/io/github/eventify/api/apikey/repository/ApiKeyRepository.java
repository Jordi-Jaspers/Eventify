package io.github.eventify.api.apikey.repository;

import io.github.eventify.api.apikey.model.ApiKey;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link ApiKey} entities.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long>, JpaSpecificationExecutor<ApiKey> {

    /**
     * Override findAll to eagerly fetch user and organization.
     *
     * @param spec     the specification
     * @param pageable the pageable
     * @return page of memberships with user and organization eagerly loaded
     */
    @NonNull
    @Override
    @EntityGraph(
        attributePaths = {
            "user",
            "organization"
        }
    )
    Page<ApiKey> findAll(@NonNull Specification<ApiKey> spec, @NonNull Pageable pageable);

    /**
     * Find all user personal API keys (organization ID is null).
     *
     * @param userId the user ID
     * @return list of personal API keys
     */
    @Query("SELECT k FROM ApiKey k LEFT JOIN FETCH k.user WHERE k.user.id = :userId AND k.organization IS NULL")
    List<ApiKey> findAllByUserIdAndOrganizationIdIsNull(@Param("userId") Long userId);

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

    /**
     * Find API key by suffix with user and organization eagerly fetched.
     *
     * @param suffix the API key suffix
     * @return Optional containing the API key if found
     */
    @Query("SELECT k FROM ApiKey k LEFT JOIN FETCH k.user LEFT JOIN FETCH k.organization WHERE k.suffix = :suffix")
    Optional<ApiKey> findBySuffix(@Param("suffix") String suffix);

    /**
     * Find an API key by ID and organization ID.
     *
     * @param id             the API key ID
     * @param organizationId the organization ID
     * @return Optional containing the API key if found
     */
    Optional<ApiKey> findByIdAndOrganizationId(Long id, Long organizationId);
}
