package io.github.eventify.api.monitor.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelGroup;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.api.monitor.model.BucketSize;
import io.github.eventify.api.monitor.model.MonitorFilters;
import io.github.eventify.api.monitor.model.MonitorResult;
import io.github.eventify.api.monitor.model.TimeSpan;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.TimelineBucket;
import io.github.eventify.api.monitor.model.TimelineDuration;
import io.github.eventify.api.monitor.model.request.MonitorRequest;
import io.github.eventify.api.monitor.repository.TimelineAggregateRepository;
import io.github.eventify.api.monitor.util.AggregateTimelineBuilder;
import io.github.eventify.api.monitor.util.LodSelector;
import io.github.eventify.api.monitor.util.TimelineBuilder;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
@SuppressWarnings(
    {
        "PMD.ExcessiveImports",
        "PMD.CouplingBetweenObjects"
    }
)
public class MonitorService {

    private static final Duration AGGREGATE_END_OFFSET = Duration.ofHours(1);

    private final WatchlistRepository watchlistRepository;

    private final ChannelRepository channelRepository;

    private final EventRepository eventRepository;

    private final TimelineAggregateRepository timelineAggregateRepository;

    /**
     * Gets monitor timeline data for a watchlist.
     *
     * @param request the monitor request
     * @return the monitor result with enriched configuration and dashboard timeline
     */
    @Transactional(readOnly = true)
    public MonitorResult monitorWatchlist(final MonitorRequest request) {
        final Watchlist watchlist = watchlistRepository.findById(request.getWatchlistId())
            .orElseThrow(() -> new DataNotFoundException(WATCHLIST_NOT_FOUND));

        final TimeSpan timeRange = watchlist.resolveTimeRange(request.getFilters());
        final MonitorFilters filters = watchlist.resolveFilters(request.getFilters());
        final WatchlistConfiguration configuration = watchlist.getConfiguration();
        final BucketSize bucketSize = LodSelector.selectBucket(timeRange);

        enrichConfiguration(configuration, timeRange, bucketSize);
        filters.apply(configuration);

        return MonitorResult.builder()
            .watchlist(watchlist)
            .timeRange(timeRange)
            .filters(filters)
            .configuration(configuration)
            .bucketSize(bucketSize)
            .build();
    }

    /**
     * Enriches the configuration with channel data and timelines.
     * Routes to aggregate or raw-event path based on the LOD bucket size.
     *
     * @param configuration the watchlist configuration to enrich
     * @param timeRange     the time span
     * @param bucketSize    the LOD bucket size (null = raw events)
     */
    private void enrichConfiguration(
        final WatchlistConfiguration configuration,
        final TimeSpan timeRange,
        final BucketSize bucketSize
    ) {
        final List<Long> allChannelIds = configuration.getAllChannelIds();
        if (allChannelIds.isEmpty()) {
            return;
        }

        final List<Channel> channels = channelRepository.findAllById(allChannelIds);
        final Map<Long, Channel> enrichedChannelsById;

        if (bucketSize == null) {
            enrichedChannelsById = enrichWithRawEvents(channels, allChannelIds, timeRange);
        } else if (timeRange.isLive()) {
            enrichedChannelsById = enrichWithAggregateAndStitch(channels, allChannelIds, timeRange, bucketSize);
        } else {
            enrichedChannelsById = enrichWithAggregate(channels, allChannelIds, timeRange, bucketSize);
        }

        final List<Channel> enrichedStandaloneChannels = configuration.getChannels().stream()
            .map(Channel::getId)
            .map(enrichedChannelsById::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
        configuration.setChannels(enrichedStandaloneChannels);
        configuration.getGroups().forEach(group -> enrichGroup(group, enrichedChannelsById));
    }

    /**
     * Raw-event path: fetches events directly for short time ranges (&lt;= 4h).
     */
    private Map<Long, Channel> enrichWithRawEvents(
        final List<Channel> channels,
        final List<Long> allChannelIds,
        final TimeSpan timeRange
    ) {
        final Map<Long, List<Event>> eventsByChannel = groupEventsByChannel(
            eventRepository.findEventsWithLastBeforeRange(allChannelIds, timeRange.getStart(), timeRange.getEnd())
        );

        channels.forEach(channel -> buildTimelineFromEvents(channel, eventsByChannel, timeRange));
        return toChannelMap(channels);
    }

    /**
     * Aggregate path for non-live ranges.
     */
    private Map<Long, Channel> enrichWithAggregate(
        final List<Channel> channels,
        final List<Long> allChannelIds,
        final TimeSpan timeRange,
        final BucketSize bucketSize
    ) {
        final Map<Long, List<TimelineBucket>> bucketsByChannel = fetchAndMergeBuckets(
            allChannelIds,
            timeRange.getStart(),
            timeRange.getEnd(),
            bucketSize
        );

        channels.forEach(channel -> buildTimelineFromBuckets(channel, bucketsByChannel, timeRange, bucketSize));
        return toChannelMap(channels);
    }

    /**
     * Aggregate path for live ranges, stitching historical aggregates with recent raw events.
     * Historical: aggregate from range start to (now - 1h).
     * Recent: raw events from (now - 1h) to range end.
     */
    private Map<Long, Channel> enrichWithAggregateAndStitch(
        final List<Channel> channels,
        final List<Long> allChannelIds,
        final TimeSpan timeRange,
        final BucketSize bucketSize
    ) {
        final OffsetDateTime historicalEnd = OffsetDateTime.now().minus(AGGREGATE_END_OFFSET);

        final Map<Long, List<TimelineBucket>> bucketsByChannel = fetchAndMergeBuckets(
            allChannelIds,
            timeRange.getStart(),
            historicalEnd,
            bucketSize
        );

        final Map<Long, List<Event>> recentEventsByChannel = groupEventsByChannel(
            eventRepository.findEventsWithLastBeforeRange(allChannelIds, historicalEnd, timeRange.getEnd())
        );

        channels.forEach(
            channel -> buildStitchedTimeline(
                channel,
                bucketsByChannel,
                recentEventsByChannel,
                timeRange,
                bucketSize,
                historicalEnd
            )
        );
        return toChannelMap(channels);
    }

    /**
     * Fetches in-range and prior buckets, then merges them into a per-channel map.
     */
    private Map<Long, List<TimelineBucket>> fetchAndMergeBuckets(
        final List<Long> channelIds,
        final OffsetDateTime start,
        final OffsetDateTime end,
        final BucketSize bucketSize
    ) {
        final List<TimelineBucket> inRangeBuckets = timelineAggregateRepository.findBucketsForChannels(
            channelIds,
            start,
            end,
            bucketSize
        );
        final List<TimelineBucket> priorBuckets = timelineAggregateRepository.findPriorBuckets(
            channelIds,
            start,
            bucketSize
        );
        return mergeBuckets(inRangeBuckets, priorBuckets);
    }

    /**
     * Merges in-range buckets with prior buckets (prior buckets at the start).
     */
    private Map<Long, List<TimelineBucket>> mergeBuckets(
        final List<TimelineBucket> inRangeBuckets,
        final List<TimelineBucket> priorBuckets
    ) {
        final Map<Long, TimelineBucket> priorByChannel = priorBuckets.stream()
            .collect(Collectors.toMap(TimelineBucket::getChannelId, Function.identity(), (a, b) -> b));

        final Map<Long, List<TimelineBucket>> result = inRangeBuckets.stream()
            .collect(Collectors.groupingBy(TimelineBucket::getChannelId, Collectors.toCollection(ArrayList::new)));

        priorByChannel.forEach(
            (channelId, prior) -> result.computeIfAbsent(channelId, k -> new ArrayList<>()).add(0, prior)
        );

        return result;
    }

    /**
     * Groups a flat list of events by channel ID.
     */
    private Map<Long, List<Event>> groupEventsByChannel(final List<Event> events) {
        return events.stream().collect(Collectors.groupingBy(e -> e.getChannel().getId()));
    }

    /**
     * Converts a list of channels to a map keyed by channel ID.
     */
    private Map<Long, Channel> toChannelMap(final List<Channel> channels) {
        return channels.stream().collect(Collectors.toMap(Channel::getId, Function.identity()));
    }

    /**
     * Enriches a channel group with its member channels.
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
     * Builds a timeline from raw events for a channel.
     */
    private void buildTimelineFromEvents(
        final Channel channel,
        final Map<Long, List<Event>> eventsByChannel,
        final TimeSpan timeRange
    ) {
        if (isPaused(channel)) {
            applyPausedTimeline(channel);
            return;
        }

        final List<Event> events = eventsByChannel.getOrDefault(channel.getId(), List.of());
        channel.setTimeline(TimelineBuilder.fromEvents(events, timeRange));
        channel.setCurrentSeverity(TimelineBuilder.getCurrentSeverity(events, timeRange.getStart(), timeRange.getEnd()));
    }

    /**
     * Builds a timeline from aggregate buckets for a channel.
     */
    private void buildTimelineFromBuckets(
        final Channel channel,
        final Map<Long, List<TimelineBucket>> bucketsByChannel,
        final TimeSpan timeRange,
        final BucketSize bucketSize
    ) {
        if (isPaused(channel)) {
            applyPausedTimeline(channel);
            return;
        }

        final List<TimelineBucket> channelBuckets = bucketsByChannel.getOrDefault(channel.getId(), List.of());
        channel.setTimeline(AggregateTimelineBuilder.fromBuckets(channelBuckets, timeRange, bucketSize));
        channel.setCurrentSeverity(null);
    }

    /**
     * Builds a stitched timeline: historical aggregates + recent raw events.
     */
    private void buildStitchedTimeline(
        final Channel channel,
        final Map<Long, List<TimelineBucket>> bucketsByChannel,
        final Map<Long, List<Event>> recentEventsByChannel,
        final TimeSpan fullRange,
        final BucketSize bucketSize,
        final OffsetDateTime historicalEnd
    ) {
        if (isPaused(channel)) {
            applyPausedTimeline(channel);
            return;
        }

        final List<TimelineBucket> channelBuckets = bucketsByChannel.getOrDefault(channel.getId(), List.of());
        final TimeSpan historicalRange = new TimeSpan(fullRange.getStart(), historicalEnd);
        final Timeline historicalTimeline = AggregateTimelineBuilder.fromBuckets(channelBuckets, historicalRange, bucketSize);

        final List<Event> recentEvents = recentEventsByChannel.getOrDefault(channel.getId(), List.of());
        final TimeSpan recentRange = new TimeSpan(historicalEnd, fullRange.getEnd());
        final Timeline recentTimeline = TimelineBuilder.fromEvents(recentEvents, recentRange);

        final List<TimelineDuration> allDurations = new ArrayList<>(historicalTimeline.getDurations());
        allDurations.addAll(recentTimeline.getDurations());
        channel.setTimeline(Timeline.builder().durations(allDurations).build());

        channel.setCurrentSeverity(
            TimelineBuilder.getCurrentSeverity(recentEvents, historicalEnd, fullRange.getEnd())
        );
    }

    private boolean isPaused(final Channel channel) {
        return channel.getStatus() == ChannelStatus.PAUSED;
    }

    private void applyPausedTimeline(final Channel channel) {
        channel.setTimeline(Timeline.empty());
        channel.setCurrentSeverity(null);
    }
}
