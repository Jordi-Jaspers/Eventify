package io.github.eventify.api.apikey.repository;

import io.github.eventify.api.apikey.model.ApiKeyAudit;

import java.time.OffsetDateTime;
import java.util.List;

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
 * Repository for {@link ApiKeyAudit} entities.
 */
@Repository
public interface ApiKeyAuditRepository extends JpaRepository<ApiKeyAudit, Long>, JpaSpecificationExecutor<ApiKeyAudit> {

    /**
     * Override findAll to eagerly fetch revokedBy user.
     *
     * @param spec     the specification
     * @param pageable the pageable
     * @return page of audit records with revokedBy eagerly loaded
     */
    @NonNull
    @Override
    @EntityGraph(attributePaths = "revokedBy")
    Page<ApiKeyAudit> findAll(@NonNull Specification<ApiKeyAudit> spec, @NonNull Pageable pageable);

    /**
     * Count keys revoked after a specific date.
     *
     * @param since the date
     * @return count of revoked keys
     */
    @Query("SELECT COUNT(a) FROM ApiKeyAudit a WHERE a.revokedAt >= :since")
    Long countByRevokedAtAfter(@Param("since") OffsetDateTime since);

    /**
     * Delete all audit records where the revoking user is in the given list.
     *
     * @param userIds the user IDs of revokers
     */
    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM ApiKeyAudit a WHERE a.revokedBy.id IN :userIds")
    void deleteAllByRevokedByIdIn(@Param("userIds") List<Long> userIds);

    /**
     * Delete all audit records where the owner user is in the given list.
     *
     * @param userIds the user IDs of owners
     */
    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM ApiKeyAudit a WHERE a.ownerUserId IN :userIds")
    void deleteAllByOwnerUserIdIn(@Param("userIds") List<Long> userIds);
}
