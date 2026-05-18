package io.github.eventify.common.audit.repository;

import io.github.eventify.api.admin.model.response.AuditLogStatsProjection;
import io.github.eventify.api.admin.model.response.HourlyBucketProjection;
import io.github.eventify.common.audit.model.AuditLog;

import java.time.OffsetDateTime;
import java.util.List;

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

/** JPA repository for audit log persistence and query operations. */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    @Query("SELECT a FROM AuditLog a WHERE a.actor.id = :actorId")
    List<AuditLog> findByActorId(@Param("actorId") Long actorId);

    @Query(
        value = """
            SELECT
                COUNT(*) AS totalRequests,
                COUNT(*) FILTER (WHERE status_code >= 400) AS errorCount,
                COUNT(*) FILTER (WHERE method IN ('POST','PUT','PATCH','DELETE')) AS mutationCount,
                COUNT(DISTINCT actor_id) AS uniqueActors
            FROM audit_log
            WHERE created_at >= :from AND created_at <= :to
            """,
        nativeQuery = true
    )
    AuditLogStatsProjection findStatsBetween(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query(
        value = """
            SELECT
                time_bucket('1 hour', created_at) AS hour,
                COUNT(*) AS total,
                COUNT(*) FILTER (WHERE status_code >= 400) AS errors
            FROM audit_log
            WHERE created_at >= :from AND created_at <= :to
            GROUP BY time_bucket('1 hour', created_at)
            ORDER BY hour ASC
            """,
        nativeQuery = true
    )
    List<HourlyBucketProjection> findHourlyBucketsBetween(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @NonNull
    @Override
    @EntityGraph(attributePaths = "actor")
    Page<AuditLog> findAll(@NonNull Specification<AuditLog> spec, @NonNull Pageable pageable);
}
