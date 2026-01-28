package io.github.eventify.api.watchlist.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Response DTO for watchlist filter settings.
 */
@Getter
@Setter
@NoArgsConstructor
public class WatchlistFiltersResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default time range for filtering events (e.g., "1h", "24h", "7d", "30d").
     */
    private String timeRange;

    /**
     * Whether to show only critical channels by default.
     * Only applied in ungrouped view mode.
     */
    private boolean onlyCritical;

    /**
     * Whether to sort by severity by default.
     * Applied in both grouped and ungrouped view modes.
     */
    private boolean sortBySeverity;

    /**
     * Whether to show grouped view by default.
     * - true: Shows channels organized in groups as configured
     * - false: Flatmaps all channels into a single list
     */
    private boolean groupedView;
}
