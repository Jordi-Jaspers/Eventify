package io.github.eventify.api.monitor.model;

import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain result from monitor service containing the enriched configuration.
 *
 * <p>The configuration contains all channels and groups with their timelines populated.
 * Filters have been applied based on the view mode (grouped/ungrouped).
 * The dashboard timeline is derived from configuration.getTimeline().
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorResult {

    /**
     * The watchlist being monitored.
     */
    private Watchlist watchlist;

    /**
     * The resolved time range for this monitor request.
     */
    private TimeSpan timeRange;

    /**
     * The resolved filters applied to this result.
     */
    private MonitorFilters filters;

    /**
     * The enriched configuration with channels and groups.
     * Contains timelines and has filters applied based on view mode.
     * Use configuration.getTimeline() to get the dashboard timeline.
     */
    private WatchlistConfiguration configuration;

}
