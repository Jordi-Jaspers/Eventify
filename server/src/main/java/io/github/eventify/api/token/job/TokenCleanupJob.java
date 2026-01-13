package io.github.eventify.api.token.job;

import io.github.eventify.api.token.service.TokenCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for processing token cleanup tasks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupJob {

    private final TokenCleanupService tokenCleanupService;

    /**
     * Process deletion of expired tokens every 24 hours.
     */
    @Scheduled(
        fixedDelay = 24,
        timeUnit = TimeUnit.HOURS
    )
    public void processExpiredTokenDeletions() {
        log.info("[CRON JOB] Token cleanup job started at {}", OffsetDateTime.now());
        tokenCleanupService.deleteExpiredTokens();
    }
}
