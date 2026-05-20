package io.github.eventify.api.watchlist.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Junction entity linking watchlists to channels.
 * Enables efficient reverse lookups: find all watchlists containing a given channel.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@IdClass(WatchlistChannelId.class)
@Table(name = "watchlist_channel")
public class WatchlistChannel {

    @Id
    @Column(
        name = "watchlist_id",
        nullable = false
    )
    private Long watchlistId;

    @Id
    @Column(
        name = "channel_id",
        nullable = false
    )
    private Long channelId;

}
