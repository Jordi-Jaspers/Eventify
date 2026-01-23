package io.github.eventify.api.watchlist.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Request DTO for creating a watchlist.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CreateWatchlistRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private String name;

    private String description;

    private WatchlistConfigurationRequest configuration;

    private WatchlistFiltersRequest filters;
}
