package io.github.eventify.api.watchlist.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Response DTO for watchlist channel details.
 */
@Getter
@Setter
@NoArgsConstructor
public class WatchlistChannelResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private Long id;

    private String name;

    private String status;

    private Integer position;
}
