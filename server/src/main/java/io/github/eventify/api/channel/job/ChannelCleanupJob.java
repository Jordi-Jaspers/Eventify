package io.github.eventify.api.channel.job;

import io.github.eventify.api.channel.service.ChannelCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Scheduled job for processing channel cleanup tasks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelCleanupJob {

    private final ChannelCleanupService channelCleanupService;

    /**
     * Process channels pending deletion every 5 minutes.
     */
    @Scheduled(
        fixedDelay = 5,
        timeUnit = MINUTES
    )
    public void processChannelDeletions() {
        log.info("[CRON JOB] Channel cleanup job started at {}", OffsetDateTime.now());
        channelCleanupService.deletePendingChannels();
    }
}
