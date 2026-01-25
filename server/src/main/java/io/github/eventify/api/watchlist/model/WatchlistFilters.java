package io.github.eventify.api.watchlist.model;

import io.github.eventify.api.monitor.model.TimeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Default filter settings for a watchlist.
 * Stored as JSONB in the database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistFilters implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default time range for filtering events.
     */
    @Builder.Default
    private TimeRange timeRange = TimeRange.LAST_24H;

    /**
     * Whether to show only critical channels by default.
     * Only applied in ungrouped view mode.
     */
    private boolean onlyCritical;

    /**
     * Whether to sort by severity by default.
     * Applied in both grouped and ungrouped view modes.
     */
    @Builder.Default
    private boolean sortBySeverity = true;

    /**
     * Whether to show channels in grouped view by default.
     * - true: Shows channels organized in groups as configured
     * - false: Flatmaps all channels (from groups and standalone) into a single list
     */
    @Builder.Default
    private boolean groupedView = true;

    /**
     * Creates default filter settings.
     *
     * @return default filters
     */
    public static WatchlistFilters defaults() {
        return WatchlistFilters.builder().build();
    }
}
