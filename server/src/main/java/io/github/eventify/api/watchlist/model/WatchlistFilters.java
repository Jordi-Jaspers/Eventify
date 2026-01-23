package io.github.eventify.api.watchlist.model;

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
     * Default time range for filtering events (e.g., "1h", "24h", "7d", "30d").
     */
    @Builder.Default
    private String timeRange = "24h";

    /**
     * Whether to show only critical events by default.
     */
    private boolean onlyCritical;

    /**
     * Whether to sort events by severity by default.
     */
    @Builder.Default
    private boolean sortBySeverity = true;

    /**
     * Creates default filter settings.
     *
     * @return default filters
     */
    public static WatchlistFilters defaults() {
        return WatchlistFilters.builder().build();
    }
}
