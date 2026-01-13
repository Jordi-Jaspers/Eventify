package io.github.eventify.api.channel.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.channel.model.ChannelStatus.PENDING_DELETION;

/**
 * Service for processing channel deletions.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChannelCleanupService {

    private final ChannelRepository channelRepository;

    /**
     * Process all channels with PENDING_DELETION status. Deletes each channel and continues processing if one fails.
     */
    public void deletePendingChannels() {
        final Instant startTime = Instant.now();
        final List<Channel> toDelete = channelRepository.findByStatus(PENDING_DELETION);
        if (CollectionUtils.isEmpty(toDelete)) {
            log.info("No channels to delete with status '{}'", PENDING_DELETION);
            return;
        }

        final long deletedCount = toDelete.stream()
            .filter(Objects::nonNull)
            .map(channel -> {
                final Instant start = Instant.now();
                try {
                    channelRepository.delete(channel);
                    log.debug("Deleted channel ID '{}' in '{}' ms", channel.getId(), Duration.between(start, Instant.now()).toMillis());
                    return 1;
                } catch (final Exception exception) {
                    log.error("Failed to delete channel ID '{}': {}", channel.getId(), exception.getMessage());
                    return 0;
                }
            })
            .mapToInt(Integer::intValue)
            .sum();

        final Duration totalDuration = Duration.between(startTime, Instant.now());
        log.info("Processed deletion of '{}' channels in '{}' ms", deletedCount, totalDuration.toMillis());
    }
}
