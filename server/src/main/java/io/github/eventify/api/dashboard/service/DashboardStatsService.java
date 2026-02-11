package io.github.eventify.api.dashboard.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.dashboard.model.response.DashboardStatsResponse;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for calculating dashboard statistics.
 */
@Service
@RequiredArgsConstructor
public class DashboardStatsService {

    private final ChannelRepository channelRepository;
    private final EventRepository eventRepository;

    /**
     * Get dashboard stats for personal channels only.
     *
     * @param userId the user ID
     * @return dashboard statistics
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getPersonalStats(final Long userId) {
        final List<Channel> activeChannels = channelRepository.findByUserIdAndOrganizationIsNullAndStatus(
            userId,
            ChannelStatus.ACTIVE
        );

        return calculateStats(activeChannels);
    }

    /**
     * Get dashboard stats for organization channels only.
     *
     * @param orgId the organization ID
     * @return dashboard statistics
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getOrganizationStats(final Long orgId) {
        final List<Channel> activeChannels = channelRepository.findByOrganizationIdAndStatus(
            orgId,
            ChannelStatus.ACTIVE
        );

        return calculateStats(activeChannels);
    }

    /**
     * Calculates dashboard stats from a list of active channels.
     *
     * @param activeChannels list of active channels
     * @return dashboard statistics
     */
    private DashboardStatsResponse calculateStats(final List<Channel> activeChannels) {
        final int activeChannelCount = activeChannels.size();

        // Extract channel IDs
        final List<Long> channelIds = activeChannels.stream()
            .map(Channel::getId)
            .toList();

        // Count events from last 24 hours
        final OffsetDateTime twentyFourHoursAgo = OffsetDateTime.now().minusHours(24);
        final long eventsToday = eventRepository.countByChannelIdInAndTimestampAfter(
            channelIds,
            twentyFourHoursAgo
        );

        // Early return for empty channels list
        if (activeChannels.isEmpty()) {
            return new DashboardStatsResponse(eventsToday, 0, 0.0, null);
        }

        // Calculate error rate based on most recent event severity per channel
        int criticalCount = 0;
        OffsetDateTime mostRecentEventTimestamp = null;

        for (final Channel channel : activeChannels) {
            final Optional<Event> lastEvent = eventRepository.findTopByChannelIdOrderByTimestampDesc(
                channel.getId()
            );

            if (lastEvent.isPresent()) {
                final Event event = lastEvent.get();

                // Check if last event is CRITICAL
                if (event.getSeverity() == Severity.CRITICAL) {
                    criticalCount++;
                }

                // Track most recent event timestamp
                if (mostRecentEventTimestamp == null
                    || event.getTimestamp().isAfter(mostRecentEventTimestamp)) {
                    mostRecentEventTimestamp = event.getTimestamp();
                }
            }
        }

        // Calculate error rate as percentage
        final double errorRate = ((double) criticalCount / activeChannelCount) * 100.0;

        return new DashboardStatsResponse(
            eventsToday,
            activeChannelCount,
            errorRate,
            mostRecentEventTimestamp
        );
    }
}
