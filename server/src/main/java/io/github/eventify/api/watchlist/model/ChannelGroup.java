package io.github.eventify.api.watchlist.model;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Severity;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Configuration for a channel group within a watchlist.
 * Stored as part of WatchlistConfiguration JSONB.
 *
 * <p>A channel group allows organizing related channels together.
 * When displayed in the monitor, the group shows a consolidated timeline
 * representing the worst severity across all member channels.
 *
 * <p>Implements {@link TimelineSource} to participate in dashboard consolidation.
 * The channels are stored as IDs in JSONB and enriched at runtime during monitor processing.
 *
 * <p>Example hierarchy:
 * <pre>
 * Watchlist Dashboard (consolidated)
 * ├── Group "API Services" (consolidated from channels 1, 2)
 * │ ├── Channel 1 (timeline)
 * │ └── Channel 2 (timeline)
 * ├── Group "Databases" (consolidated from channels 3, 4)
 * │ ├── Channel 3 (timeline)
 * │ └── Channel 4 (timeline)
 * └── Channel 5 (standalone, timeline)
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelGroup implements Serializable, TimelineSource {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Unique identifier for this group within the watchlist.
     * Generated client-side when creating the group.
     */
    private UUID id;

    /**
     * Display name for the group.
     */
    private String name;

    /**
     * Ordered list of channels belonging to this group.
     * Initially contains only IDs (from JSONB), enriched at runtime with full channel data.
     */
    @JsonIgnore
    @Builder.Default
    private List<Channel> channels = new ArrayList<>();

    /**
     * Creates a new group with generated ID.
     *
     * @param name     the group name
     * @param channels the channels (can have only id populated)
     * @return a new channel group
     */
    public static ChannelGroup of(final String name, final List<Channel> channels) {
        return ChannelGroup.builder()
            .id(UUID.randomUUID())
            .name(name)
            .channels(new ArrayList<>(channels))
            .build();
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
            .map(ChannelGroup::channelWithId)
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns the consolidated timeline for this group.
     * The timeline shows the worst severity at each point in time across all member channels.
     *
     * @return the consolidated timeline, never null
     */
    @Override
    @JsonIgnore
    public Timeline getTimeline() {
        if (channels == null || channels.isEmpty()) {
            return Timeline.empty();
        }
        return TimelineConsolidator.consolidate(channels);
    }

    /**
     * Returns the current (worst) severity across all member channels.
     * Used for sorting groups by severity.
     *
     * @return the worst severity, or null if no channels have severity data
     */
    @JsonIgnore
    public Severity getCurrentSeverity() {
        if (channels == null || channels.isEmpty()) {
            return null;
        }
        return channels.stream()
            .map(Channel::getCurrentSeverity)
            .filter(s -> s != null)
            .min(Comparator.comparingInt(Severity::getPriority))
            .orElse(null);
    }

    /**
     * Creates a Channel with only the id populated.
     */
    private static Channel channelWithId(final Long id) {
        final Channel channel = new Channel();
        channel.setId(id);
        return channel;
    }
}
