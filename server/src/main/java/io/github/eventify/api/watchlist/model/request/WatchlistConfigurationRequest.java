package io.github.eventify.api.watchlist.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Request DTO for watchlist configuration containing channel IDs.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class WatchlistConfigurationRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Ordered list of channel IDs.
     */
    private List<Long> channelIds;
}
