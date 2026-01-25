package io.github.eventify.api.watchlist.model.request;

import io.github.eventify.api.monitor.model.TimeRange;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Request DTO for watchlist filter settings.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class WatchlistFiltersRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default time range for filtering events.
     */
    private TimeRange timeRange;

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
