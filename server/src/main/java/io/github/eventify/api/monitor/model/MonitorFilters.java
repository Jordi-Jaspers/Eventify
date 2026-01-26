package io.github.eventify.api.monitor.model;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelGroup;
import io.github.eventify.api.event.model.Severity;
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
        description = "Show only channels with CRITICAL severity. In grouped view, empty groups are removed.",
        example = "false"
    )
    private Boolean onlyCritical;

    @Schema(
        description = "Sort channels by severity (CRITICAL first). Groups remain in configured order.",
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
     * Returns a predicate to filter channels by critical severity.
     *
     * @return Predicate that matches only CRITICAL channels when onlyCritical is true
     */
    public Predicate<Channel> criticalFilter() {
        if (Boolean.TRUE.equals(this.getOnlyCritical())) {
            return ch -> ch.getCurrentSeverity() == Severity.CRITICAL;
        }
        return ch -> true;
    }

    /**
     * Applies filters and sorting to the configuration based on view mode.
     * Modifies the configuration in-place.
     *
     * <p>Filter application rules:
     * <ul>
     * <li>onlyCritical: Applied in both views. In grouped view, empty groups are removed.</li>
     * <li>sortBySeverity: Sorts channels within groups and standalone channels. Groups remain in configured order.</li>
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
     * - Groups remain in configured order (NO sorting of groups)
     * - Filters channels within each group by critical severity (if enabled)
     * - Sorts channels within each group by severity (if enabled)
     * - Removes empty groups after filtering
     * - Filters and sorts standalone channels
     */
    private void applyGroupedView(final WatchlistConfiguration configuration) {
        final Predicate<Channel> filter = this.criticalFilter();

        // Process each group: filter then sort channels within
        final List<ChannelGroup> processedGroups = configuration.getGroups().stream()
            .map(group -> {
                final List<Channel> filteredChannels = group.getChannels().stream()
                    .filter(filter)
                    .sorted(this.channelComparator())
                    .toList();
                group.setChannels(filteredChannels);
                return group;
            })
            // Remove groups that have no channels after filtering
            .filter(group -> !group.getChannels().isEmpty())
            .collect(Collectors.toCollection(ArrayList::new));
        configuration.setGroups(processedGroups);

        // Filter and sort standalone channels
        final List<Channel> filteredChannels = configuration.getChannels().stream()
            .filter(filter)
            .sorted(this.channelComparator())
            .collect(Collectors.toCollection(ArrayList::new));
        configuration.setChannels(filteredChannels);
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

        // Apply filter and sort
        final List<Channel> filteredChannels = allChannels.stream()
            .filter(this.criticalFilter())
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
