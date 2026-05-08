package io.github.eventify.api.quota.job;

import io.github.eventify.api.quota.service.UserQuotaCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for processing user quota cleanup tasks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserQuotaCleanupJob {

    private final UserQuotaCleanupService userQuotaCleanupService;

    /**
     * Reset all user quotas on the 1st of every month at midnight.
     */
    @Scheduled(cron = "0 0 0 1 * *")
    public void processExpiredTokenDeletions() {
        log.info("[CRON JOB] User quota cleanup job started at {}", OffsetDateTime.now());
        userQuotaCleanupService.resetMonthlyQuotas();
    }
}
