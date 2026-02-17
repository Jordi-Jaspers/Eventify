package io.github.eventify.api.channel.job;

import io.github.eventify.api.channel.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static io.github.eventify.common.util.TimeProvider.now;

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
     * Marks channels as stale if they haven't received events in 7 days.
     * Runs every 5 minutes for responsive staleness detection.
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void markStaleChannels() {
        log.info("[CRON JOB] Channel staleness job started at {}", now());

        final OffsetDateTime threshold = now().minusDays(STALENESS_THRESHOLD_DAYS);
        final int markedCount = channelRepository.markChannelsAsStale(threshold, threshold);

        log.info("[CRON JOB] Channel staleness job completed. Marked {} channels as stale.", markedCount);
    }
}
