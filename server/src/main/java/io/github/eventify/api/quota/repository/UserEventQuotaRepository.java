package io.github.eventify.api.quota.repository;

import io.github.eventify.api.quota.model.UserEventQuota;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
     * Find quota record by user ID with pessimistic write lock. Prevents race conditions during quota checks.
     *
     * @param userId the user ID
     * @return optional quota record
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM UserEventQuota q WHERE q.user.id = :userId")
    Optional<UserEventQuota> findByUserIdWithLock(@Param("userId") Long userId);

    /**
     * Reset all user quotas to 0 for new period.
     *
     * @param periodStart the new period start date
     * @return number of quotas reset
     */
    @Modifying
    @Query("UPDATE UserEventQuota q SET q.eventCount = 0, q.periodStart = :periodStart")
    int resetAllQuotas(@Param("periodStart") OffsetDateTime periodStart);

    /**
     * Count users with event_count >= nearLimitThreshold AND < atLimitThreshold.
     *
     * @param nearLimitThreshold lower bound (inclusive)
     * @param atLimitThreshold   upper bound (exclusive)
     * @return count of users near limit
     */
    @Query("SELECT COUNT(q) FROM UserEventQuota q WHERE q.eventCount >= :nearLimitThreshold AND q.eventCount < :atLimitThreshold")
    Long countUsersNearLimit(@Param("nearLimitThreshold") int nearLimitThreshold, @Param("atLimitThreshold") int atLimitThreshold);

    /**
     * Count users with event_count >= monthlyLimit (at or over limit).
     *
     * @param monthlyLimit the monthly event limit
     * @return count of users at or over limit
     */
    @Query("SELECT COUNT(q) FROM UserEventQuota q WHERE q.eventCount >= :monthlyLimit")
    Long countUsersAtLimit(@Param("monthlyLimit") int monthlyLimit);

    /**
     * Calculate average quota utilization as a percentage (avg(event_count) / monthlyLimit * 100).
     *
     * @param monthlyLimit the monthly event limit
     * @return average utilization percentage, or 0.0 if no records
     */
    @Query("SELECT COALESCE(AVG(q.eventCount) * 100.0 / :monthlyLimit, 0.0) FROM UserEventQuota q")
    Double calculateAverageUtilization(@Param("monthlyLimit") int monthlyLimit);

    /**
     * Delete all quotas for users with the given IDs.
     *
     * @param userIds the user IDs
     */
    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM UserEventQuota q WHERE q.user.id IN :userIds")
    void deleteAllByUserIdIn(@Param("userIds") List<Long> userIds);
}
