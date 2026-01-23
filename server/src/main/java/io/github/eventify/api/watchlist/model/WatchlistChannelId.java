package io.github.eventify.api.watchlist.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Composite primary key for WatchlistChannel entity.
 */
@Getter
@Setter
@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class WatchlistChannelId implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Column(name = "watchlist_id")
    private Long watchlistId;

    @Column(name = "channel_id")
    private Long channelId;

    /**
     * Creates a composite key for WatchlistChannel.
     *
     * @param watchlistId the watchlist ID
     * @param channelId   the channel ID
     */
    public WatchlistChannelId(final Long watchlistId, final Long channelId) {
        this.watchlistId = watchlistId;
        this.channelId = channelId;
    }
}
