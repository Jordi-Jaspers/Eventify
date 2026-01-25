package io.github.eventify.api.monitor.model;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.watchlist.model.ChannelGroup;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Filter settings for monitor requests and responses.
 * In requests, null values mean "use watchlist defaults".
 * In responses, values are always resolved (never null).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Monitor filter settings")
public class MonitorFilters {

    @Schema(
        description = "Preset time range (mutually exclusive with custom range)",
        example = "LAST_24H",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private TimeRange timeRange;

    @Schema(
        description = "Custom range start time (requires endTime)",
        example = "2026-01-20T10:00:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime startTime;

    @Schema(
        description = "Custom range end time (requires startTime)",
        example = "2026-01-24T10:00:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime endTime;

    @Schema(
        description = "Show only channels with CRITICAL severity (only in ungrouped view)",
        example = "false"
    )
    private Boolean onlyCritical;

    @Schema(
        description = "Sort by severity (CRITICAL first). Applied to channels and groups.",
        example = "true"
    )
    private Boolean sortBySeverity;

    @Schema(
        description = "Show grouped view (true) or ungrouped/flat view (false)",
        example = "true"
    )
    private Boolean groupedView;

    /**
     * Provides a comparator for channels based on the sortBySeverity setting.
     *
     * @return Comparator for Channel objects
     */
    public Comparator<Channel> channelComparator() {
        if (Boolean.TRUE.equals(this.getSortBySeverity())) {
            return Comparator.comparingInt(
                ch -> ch.getCurrentSeverity() != null
                    ? ch.getCurrentSeverity().getPriority()
                    : Severity.NO_DATA.getPriority()
            );
        }
        return Comparator.comparingLong(Channel::getId);
    }

    /**
     * Provides a comparator for channel groups based on the sortBySeverity setting.
     * Groups are sorted by their consolidated timeline's worst severity.
     *
     * @return Comparator for ChannelGroup objects
     */
    public Comparator<ChannelGroup> groupComparator() {
        if (Boolean.TRUE.equals(this.getSortBySeverity())) {
            return Comparator.comparingInt(
                group -> group.getCurrentSeverity() != null
                    ? group.getCurrentSeverity().getPriority()
                    : Severity.NO_DATA.getPriority()
            );
        }
        return Comparator.comparing(group -> group.getName() != null ? group.getName() : "");
    }

    /**
     * Applies filters and sorting to the configuration based on view mode.
     * Modifies the configuration in-place.
     *
     * <p>Filter application rules:
     * <ul>
     * <li>onlyCritical: Only applied in ungrouped view</li>
     * <li>sortBySeverity: Applied in both views (groups, channels within groups, standalone channels)</li>
     * <li>groupedView=false: Flatmaps all channels from groups into the channels list, clears groups</li>
     * </ul>
     *
     * @param configuration the configuration to apply filters to
     */
    public void apply(final WatchlistConfiguration configuration) {
        if (Boolean.FALSE.equals(this.getGroupedView())) {
            applyUngroupedView(configuration);
        } else {
            applyGroupedView(configuration);
        }
    }

    /**
     * Applies filters for grouped view mode.
     * - Sorts groups by severity (if enabled)
     * - Sorts channels within each group by severity (if enabled)
     * - Sorts standalone channels by severity (if enabled)
     * - Does NOT apply onlyCritical filter
     */
    private void applyGroupedView(final WatchlistConfiguration configuration) {
        // Sort channels within each group
        configuration.getGroups().forEach(group -> {
            final List<Channel> sortedChannels = group.getChannels().stream()
                .sorted(this.channelComparator())
                .toList();
            group.setChannels(sortedChannels);
        });

        // Sort groups by their consolidated severity
        final List<ChannelGroup> sortedGroups = configuration.getGroups().stream()
            .sorted(this.groupComparator())
            .collect(Collectors.toCollection(ArrayList::new));
        configuration.setGroups(sortedGroups);

        // Sort standalone channels
        final List<Channel> sortedChannels = configuration.getChannels().stream()
            .sorted(this.channelComparator())
            .collect(Collectors.toCollection(ArrayList::new));
        configuration.setChannels(sortedChannels);
    }

    /**
     * Applies filters for ungrouped view mode.
     * - Flatmaps all channels from groups into the channels list
     * - Clears the groups list
     * - Applies onlyCritical filter (if enabled)
     * - Sorts all channels by severity (if enabled)
     */
    private void applyUngroupedView(final WatchlistConfiguration configuration) {
        // Collect all channels: standalone + all from groups
        final List<Channel> allChannels = Stream.concat(
            configuration.getChannels().stream(),
            configuration.getGroups().stream()
                .flatMap(group -> group.getChannels().stream())
        ).collect(Collectors.toList());

        // Apply onlyCritical filter (only in ungrouped view)
        final Predicate<Channel> severityFilter = Boolean.TRUE.equals(this.getOnlyCritical())
            ? ch -> ch.getCurrentSeverity() == Severity.CRITICAL
            : ch -> true;

        final List<Channel> filteredChannels = allChannels.stream()
            .filter(severityFilter)
            .sorted(this.channelComparator())
            .collect(Collectors.toCollection(ArrayList::new));

        configuration.setChannels(filteredChannels);
        configuration.setGroups(new ArrayList<>());
    }

    /**
     * Creates filters with resolved boolean values (for response).
     *
     * @param onlyCritical   whether to show only critical channels
     * @param sortBySeverity whether to sort by severity
     * @param groupedView    whether to show grouped view
     * @return resolved MonitorFilters
     */
    public static MonitorFilters resolved(
        final boolean onlyCritical,
        final boolean sortBySeverity,
        final boolean groupedView
    ) {
        return MonitorFilters.builder()
            .onlyCritical(onlyCritical)
            .sortBySeverity(sortBySeverity)
            .groupedView(groupedView)
            .build();
    }
}
