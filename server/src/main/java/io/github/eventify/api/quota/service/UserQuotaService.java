package io.github.eventify.api.quota.service;

import io.github.eventify.api.quota.model.UserEventQuota;
import io.github.eventify.api.quota.model.response.UserQuotaResponse;
import io.github.eventify.api.quota.repository.UserEventQuotaRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.exception.QuotaExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.time.ZoneOffset.UTC;

/**
 * Service for managing user event quota.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQuotaService {

    public static final int MONTHLY_EVENT_LIMIT = 1000;

    private final UserEventQuotaRepository quotaRepository;

    private final UserService userService;

    /**
     * Check quota and increment atomically. Throws QuotaExceededException if quota exceeded.
     *
     * @param userId     the user ID
     * @param eventCount the number of events to increment
     * @throws QuotaExceededException if quota would be exceeded
     */
    @Transactional
    public void checkAndIncrementOrThrow(final Long userId, final int eventCount) {
        final UserEventQuota quota = getOrCreateQuotaWithLock(userId);
        final int newCount = quota.getEventCount() + eventCount;

        if (newCount > MONTHLY_EVENT_LIMIT) {
            throw new QuotaExceededException(MONTHLY_EVENT_LIMIT, quota.getEventCount(), getNextResetDate());
        }

        quota.setEventCount(newCount);
        quotaRepository.save(quota);
    }

    /**
     * Get quota status for user.
     *
     * @param userId the user ID
     * @return quota response DTO
     */
    @Transactional
    public UserQuotaResponse getQuotaStatus(final Long userId) {
        final UserEventQuota quota = getOrCreateQuotaWithLock(userId);
        final Integer used = quota.getEventCount();
        final Integer remaining = Math.max(0, MONTHLY_EVENT_LIMIT - used);
        final Double percentUsed = Math.min(100.0, ((double) used / MONTHLY_EVENT_LIMIT) * 100.0);

        final OffsetDateTime periodStart = quota.getPeriodStart();
        final OffsetDateTime periodEnd = periodStart.plusMonths(1).with(LocalTime.MIN);

        return new UserQuotaResponse()
            .setUsed(used)
            .setLimit(MONTHLY_EVENT_LIMIT)
            .setRemaining(remaining)
            .setPeriodStart(periodStart.toLocalDate())
            .setPeriodEnd(periodEnd.toLocalDate())
            .setPercentUsed(percentUsed);
    }

    /**
     * Get or create quota record for user with pessimistic lock (for write operations).
     *
     * @param userId the user ID
     * @return the quota record
     */
    public UserEventQuota getOrCreateQuotaWithLock(final Long userId) {
        final User user = userService.findById(userId);
        return quotaRepository.findByUserIdWithLock(userId)
            .orElseGet(() -> quotaRepository.save(new UserEventQuota(user)));
    }

    /**
     * Get the next reset date (1st of next month at midnight UTC).
     *
     * @return the next reset date
     */
    public OffsetDateTime getNextResetDate() {
        return OffsetDateTime.now(UTC)
            .plusMonths(1)
            .withDayOfMonth(1)
            .with(LocalTime.MIN);
    }
}
