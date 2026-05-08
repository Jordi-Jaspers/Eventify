package io.github.eventify.api.event.job;

import io.github.eventify.api.event.service.EventRetentionCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.HOURS;

/**
 * Scheduled job for cleaning up expired events based on retention policies.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventRetentionCleanupJob {

    private final EventRetentionCleanupService eventRetentionCleanupService;

    /**
     * Clean up expired events at startup and every 24 hours.
     */
    @Scheduled(
        fixedDelay = 24,
        timeUnit = HOURS
    )
    public void cleanupExpiredEvents() {
        log.info("[CRON JOB] Event retention cleanup job started at {}", OffsetDateTime.now());
        eventRetentionCleanupService.cleanupExpiredEvents();
    }
}
