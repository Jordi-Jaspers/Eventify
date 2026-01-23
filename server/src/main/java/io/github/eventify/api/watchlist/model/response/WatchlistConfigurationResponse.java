package io.github.eventify.api.watchlist.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Response DTO for watchlist configuration.
 */
@Getter
@Setter
@NoArgsConstructor
public class WatchlistConfigurationResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Ordered list of channel IDs.
     */
    private List<Long> channelIds;
}
