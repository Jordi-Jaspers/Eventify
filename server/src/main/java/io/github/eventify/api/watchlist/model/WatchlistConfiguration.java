package io.github.eventify.api.watchlist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Configuration for a watchlist containing channel IDs.
 * Stored as JSONB in the database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistConfiguration implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Ordered list of channel IDs.
     */
    @Builder.Default
    private List<Long> channelIds = new ArrayList<>();

    /**
     * Creates a default empty configuration.
     *
     * @return a new empty configuration
     */
    public static WatchlistConfiguration empty() {
        return WatchlistConfiguration.builder().build();
    }
}
