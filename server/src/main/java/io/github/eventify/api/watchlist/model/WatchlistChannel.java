package io.github.eventify.api.watchlist.model;

import io.github.eventify.api.channel.model.Channel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import jakarta.persistence.*;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Entity representing a watchlist-channel association with position ordering.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "watchlist_channel")
public class WatchlistChannel implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @EmbeddedId
    private WatchlistChannelId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("watchlistId")
    @JoinColumn(name = "watchlist_id")
    private Watchlist watchlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("channelId")
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(
        name = "position",
        nullable = false
    )
    private Integer position;

    /**
     * Creates a new watchlist-channel association.
     *
     * @param watchlist the watchlist
     * @param channel   the channel
     * @param position  the display position
     */
    public WatchlistChannel(final Watchlist watchlist, final Channel channel, final int position) {
        this.id = new WatchlistChannelId(watchlist.getId(), channel.getId());
        this.watchlist = watchlist;
        this.channel = channel;
        this.position = position;
    }
}
