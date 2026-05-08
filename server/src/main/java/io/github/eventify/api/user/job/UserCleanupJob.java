package io.github.eventify.api.user.job;

import io.github.eventify.api.user.service.UserCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * Scheduled job for processing user cleanup tasks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupJob {

    private final UserCleanupService userCleanupService;

    /**
     * Process deletion of expired unvalidated accounts every 24 hours.
     */
    @Scheduled(
        fixedDelay = 24,
        timeUnit = TimeUnit.HOURS
    )
    public void processExpiredUnvalidatedAccountDeletions() {
        log.info("[CRON JOB] User cleanup job started at {}", OffsetDateTime.now());
        userCleanupService.deleteExpiredUnvalidatedAccounts();
    }
}
