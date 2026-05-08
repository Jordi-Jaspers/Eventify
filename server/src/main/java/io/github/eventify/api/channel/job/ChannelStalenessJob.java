package io.github.eventify.api.channel.job;

import io.github.eventify.api.channel.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static io.github.eventify.common.util.TimeProvider.now;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Scheduled job for marking stale channels.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelStalenessJob {

    private static final int STALENESS_THRESHOLD_DAYS = 7;

    private final ChannelRepository channelRepository;

    /**
     * Updates channel staleness status.
     * - Marks channels as stale if no events in 7 days
     * - Clears stale flag for channels with recent activity (safety net for trigger bypass)
     * Runs at startup and every 5 minutes for responsive staleness detection.
     */
    @Scheduled(
        fixedDelay = 5,
        timeUnit = MINUTES
    )
    public void markStaleChannels() {
        log.info("[CRON JOB] Channel staleness job started at {}", now());

        final OffsetDateTime threshold = now().minusDays(STALENESS_THRESHOLD_DAYS);

        final int markedCount = channelRepository.markChannelsAsStale(threshold, threshold);
        final int clearedCount = channelRepository.clearStaleForActiveChannels(threshold);

        log.info("[CRON JOB] Channel staleness job completed. Marked {} as stale, cleared {} from stale.", markedCount, clearedCount);
    }
}
