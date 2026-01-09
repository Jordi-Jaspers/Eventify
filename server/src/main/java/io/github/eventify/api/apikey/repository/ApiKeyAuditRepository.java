package io.github.eventify.api.apikey.repository;

import io.github.eventify.api.apikey.model.ApiKeyAudit;

import java.time.OffsetDateTime;

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
}
