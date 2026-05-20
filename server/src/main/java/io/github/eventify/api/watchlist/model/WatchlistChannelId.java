package io.github.eventify.api.watchlist.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Composite primary key for {@link WatchlistChannel}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WatchlistChannelId implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private Long watchlistId;

    private Long channelId;

}
