package io.github.eventify.api.monitor.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.api.monitor.model.MonitorFilters;
import io.github.eventify.api.monitor.model.MonitorResult;
import io.github.eventify.api.monitor.model.TimeSpan;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.request.MonitorRequest;
import io.github.eventify.api.monitor.util.TimelineBuilder;
import io.github.eventify.api.watchlist.model.ChannelGroup;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.WATCHLIST_NOT_FOUND;

/**
 * Service for monitor timeline aggregation. Orchestrates data fetching and delegates timeline building to utility classes.
 */
@Service
@RequiredArgsConstructor
public class MonitorService {

    private final WatchlistRepository watchlistRepository;

    private final ChannelRepository channelRepository;

    private final EventRepository eventRepository;

    /**
     * Gets monitor timeline data for a watchlist.
     *
     * @param watchlistId the watchlist ID
     * @param request     the monitor request
     * @return the monitor result with enriched configuration and dashboard timeline
     */
    @Transactional(readOnly = true)
    public MonitorResult monitorWatchlist(final Long watchlistId, final MonitorRequest request) {
        final Watchlist watchlist = watchlistRepository.findById(watchlistId)
            .orElseThrow(() -> new DataNotFoundException(WATCHLIST_NOT_FOUND));

        final TimeSpan timeRange = watchlist.resolveTimeRange(request.getFilters());
        final MonitorFilters filters = watchlist.resolveFilters(request.getFilters());
        final WatchlistConfiguration configuration = watchlist.getConfiguration();

        enrichConfiguration(configuration, timeRange);
        filters.apply(configuration);

        return MonitorResult.builder()
            .watchlist(watchlist)
            .timeRange(timeRange)
            .filters(filters)
            .configuration(configuration)
            .build();
    }

    /**
     * Enriches the configuration with channel data and timelines.
     * Fetches all channels, builds their timelines, and updates the configuration in-place.
     *
     * @param configuration the watchlist configuration to enrich
     * @param timeRange     the time span
     */
    private void enrichConfiguration(final WatchlistConfiguration configuration, final TimeSpan timeRange) {
        final List<Long> allChannelIds = configuration.getAllChannelIds();
        if (allChannelIds.isEmpty()) {
            return;
        }

        final List<Channel> channels = channelRepository.findAllById(allChannelIds);
        final Map<Long, List<Event>> eventsByChannel = eventRepository
            .findEventsWithLastBeforeRange(allChannelIds, timeRange.getStart(), timeRange.getEnd())
            .stream()
            .collect(Collectors.groupingBy(e -> e.getChannel().getId()));

        channels.forEach(channel -> enrichChannelTimeline(channel, eventsByChannel, timeRange));
        final Map<Long, Channel> enrichedChannelsById = channels.stream()
            .collect(Collectors.toMap(Channel::getId, Function.identity()));

        final List<Channel> enrichedStandaloneChannels = configuration.getChannels().stream()
            .map(Channel::getId)
            .map(enrichedChannelsById::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(java.util.ArrayList::new));
        configuration.setChannels(enrichedStandaloneChannels);
        configuration.getGroups().forEach(group -> enrichGroup(group, enrichedChannelsById));
    }

    /**
     * Enriches a channel group with its member channels.
     * Replaces the ID-only channels with fully enriched ones.
     */
    private void enrichGroup(final ChannelGroup group, final Map<Long, Channel> enrichedChannelsById) {
        final List<Channel> enrichedChannels = group.getChannels().stream()
            .map(Channel::getId)
            .map(enrichedChannelsById::get)
            .filter(Objects::nonNull)
            .toList();

        group.setChannels(enrichedChannels);
    }

    /**
     * Enriches a single channel with its timeline.
     */
    private void enrichChannelTimeline(final Channel channel, final Map<Long, List<Event>> eventsByChannel, final TimeSpan timeRange) {
        if (channel.getStatus() == ChannelStatus.PAUSED) {
            channel.setTimeline(Timeline.empty());
            channel.setCurrentSeverity(null);
            return;
        }

        final List<Event> events = eventsByChannel.getOrDefault(channel.getId(), List.of());
        channel.setTimeline(TimelineBuilder.fromEvents(events, timeRange));
        channel.setCurrentSeverity(TimelineBuilder.getCurrentSeverity(events, timeRange.getStart(), timeRange.getEnd()));
    }
}
