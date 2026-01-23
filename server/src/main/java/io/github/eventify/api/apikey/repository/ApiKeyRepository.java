package io.github.eventify.api.apikey.repository;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyScope;

import java.time.OffsetDateTime;
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

    /**
     * Count all active API keys.
     *
     * @return count of all keys
     */
    @Query("SELECT COUNT(k) FROM ApiKey k")
    Long countAllKeys();

    /**
     * Count API keys by scope.
     *
     * @param scope the scope
     * @return count of keys with given scope
     */
    Long countByScope(ApiKeyScope scope);

    /**
     * Count keys created after a specific date.
     *
     * @param since the date
     * @return count of keys
     */
    Long countByCreatedAtAfter(OffsetDateTime since);

    /**
     * Count keys expiring between two dates (and not null).
     *
     * @param start the start date
     * @param end   the end date
     * @return count of keys
     */
    @Query("SELECT COUNT(k) FROM ApiKey k WHERE k.expiresAt IS NOT NULL AND k.expiresAt BETWEEN :start AND :end")
    Long countByExpiresAtBetween(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    /**
     * Count keys that have never been used.
     *
     * @return count of keys
     */
    Long countByLastUsedAtIsNull();

    /**
     * Find top N keys by usage.
     *
     * @param pageable the pageable
     * @return list of top keys
     */
    @Query("SELECT k FROM ApiKey k LEFT JOIN FETCH k.user LEFT JOIN FETCH k.organization ORDER BY k.totalRequests DESC")
    List<ApiKey> findTopByOrderByTotalRequestsDesc(Pageable pageable);

    /**
     * Delete all API keys owned by users with the given IDs.
     *
     * @param userIds the user IDs
     */
    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM ApiKey k WHERE k.user.id IN :userIds")
    void deleteAllByUserIdIn(@Param("userIds") List<Long> userIds);
}
