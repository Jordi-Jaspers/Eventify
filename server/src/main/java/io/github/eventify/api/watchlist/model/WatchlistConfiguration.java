package io.github.eventify.api.watchlist.model;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.TimelineSource;
import io.github.eventify.api.monitor.util.TimelineConsolidator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Configuration for a watchlist containing channels and channel groups.
 * Stored as JSONB in the database.
 *
 * <p>The configuration supports a hierarchical structure:
 * <ul>
 * <li>Standalone channels - appear directly on the dashboard</li>
 * <li>Channel groups - organize related channels, show consolidated timeline</li>
 * </ul>
 *
 * <p>Implements {@link TimelineSource} to enable dashboard-level timeline consolidation.
 * The configuration's timeline represents the worst severity across all channels and groups.
 * This design supports future nested groups (groups containing groups).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistConfiguration implements Serializable, TimelineSource {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Ordered list of standalone channels (not in any group).
     * Initially contains only IDs (from JSONB), enriched at runtime with full channel data.
     */
    @JsonIgnore
    @Builder.Default
    private List<Channel> channels = new ArrayList<>();

    /**
     * Ordered list of channel groups.
     */
    @Builder.Default
    private List<ChannelGroup> groups = new ArrayList<>();

    /**
     * Creates a default empty configuration.
     *
     * @return a new empty configuration
     */
    public static WatchlistConfiguration empty() {
        return WatchlistConfiguration.builder().build();
    }

    /**
     * Gets channel IDs for JSON serialization.
     * Extracts IDs from the channels list.
     */
    @JsonProperty("channelIds")
    public List<Long> getChannelIds() {
        if (channels == null) {
            return List.of();
        }
        return channels.stream()
            .map(Channel::getId)
            .toList();
    }

    /**
     * Sets channels from IDs during JSON deserialization.
     * Creates Channel objects with only the id field populated.
     */
    @JsonProperty("channelIds")
    public void setChannelIds(final List<Long> channelIds) {
        if (channelIds == null) {
            this.channels = new ArrayList<>();
            return;
        }
        this.channels = channelIds.stream()
            .map(Channel::new)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns all channel IDs from both standalone channels and groups.
     * Useful for batch fetching all channels.
     *
     * @return all channel IDs
     */
    @JsonIgnore
    public List<Long> getAllChannelIds() {
        final List<Long> standaloneIds = getChannelIds();
        final List<Long> groupChannelIds = groups.stream()
            .flatMap(g -> g.getChannelIds().stream())
            .toList();

        return Stream.concat(standaloneIds.stream(), groupChannelIds.stream())
            .distinct()
            .toList();
    }

    /**
     * Returns all timeline sources in this configuration.
     * Includes standalone channels and groups (which consolidate their own channels).
     * This design supports future nested groups.
     *
     * @return list of timeline sources
     */
    @JsonIgnore
    public List<TimelineSource> getTimelineSources() {
        final List<TimelineSource> sources = new ArrayList<>();
        sources.addAll(channels);
        sources.addAll(groups);
        return sources;
    }

    /**
     * Returns the consolidated timeline for this configuration.
     * The timeline shows the worst severity at each point in time across all channels and groups.
     *
     * @return the consolidated timeline, never null
     */
    @Override
    @JsonIgnore
    public Timeline getTimeline() {
        return TimelineConsolidator.consolidate(getTimelineSources());
    }
}
