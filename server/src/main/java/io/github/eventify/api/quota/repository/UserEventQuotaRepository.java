package io.github.eventify.api.quota.repository;

import io.github.eventify.api.quota.model.UserEventQuota;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for user event quota operations.
 */
@Repository
public interface UserEventQuotaRepository extends JpaRepository<UserEventQuota, Long> {

    /**
     * Find quota record by user ID.
     *
     * @param userId the user ID
     * @return optional quota record
     */
    Optional<UserEventQuota> findByUserId(Long userId);

    /**
     * Reset all user quotas to 0 for new period.
     *
     * @param periodStart the new period start date
     * @return number of quotas reset
     */
    @Modifying
    @Query("UPDATE UserEventQuota q SET q.eventCount = 0, q.periodStart = :periodStart")
    int resetAllQuotas(@Param("periodStart") OffsetDateTime periodStart);
}
