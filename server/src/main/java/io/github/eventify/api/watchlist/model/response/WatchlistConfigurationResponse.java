package io.github.eventify.api.watchlist.model.response;

import io.github.eventify.api.channel.model.response.ChannelGroupResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Response DTO for watchlist configuration.
 *
 * <p>Contains both standalone channels and channel groups.
 */
@Getter
@Setter
@NoArgsConstructor
public class WatchlistConfigurationResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Ordered list of standalone channel IDs (not in any group).
     */
    private List<Long> channelIds;

    /**
     * Ordered list of channel groups.
     */
    private List<ChannelGroupResponse> groups;
}
