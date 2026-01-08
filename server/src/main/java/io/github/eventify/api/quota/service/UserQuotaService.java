package io.github.eventify.api.quota.service;

import io.github.eventify.api.quota.model.UserEventQuota;
import io.github.eventify.api.quota.model.response.UserQuotaResponse;
import io.github.eventify.api.quota.repository.UserEventQuotaRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Scheduled;
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
     * Reset all user quotas on the 1st of every month at midnight. Runs automatically via Spring's task scheduler.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlyQuotas() {
        final OffsetDateTime periodStart = OffsetDateTime.now(UTC)
            .withDayOfMonth(1)
            .with(LocalTime.MIN);
        final int resetCount = quotaRepository.resetAllQuotas(periodStart);
        log.info("Monthly quota reset: {} user quotas reset to 0", resetCount);
    }

    /**
     * Check if user can send an event (under quota).
     *
     * @param userId the user ID
     * @return true if under limit, false if at or over limit
     */
    @Transactional
    public boolean canSendEvent(final Long userId) {
        final UserEventQuota quota = getOrCreateQuota(userId);
        return quota.getEventCount() < MONTHLY_EVENT_LIMIT;
    }

    /**
     * Increment event usage count for user.
     *
     * @param userId the user ID
     */
    @Transactional
    public void incrementUsage(final Long userId) {
        final UserEventQuota quota = getOrCreateQuota(userId);
        quota.setEventCount(quota.getEventCount() + 1);
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
        final UserEventQuota quota = getOrCreateQuota(userId);
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
     * Get or create quota record for user.
     *
     * @param userId the user ID
     * @return the quota record
     */
    private UserEventQuota getOrCreateQuota(final Long userId) {
        final User user = userService.findById(userId);
        return quotaRepository.findByUserId(userId)
            .orElseGet(() -> quotaRepository.save(new UserEventQuota(user)));
    }
}
