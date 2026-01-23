package io.github.eventify.api.watchlist.model.response;

import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Response DTO for watchlist details.
 */
@Getter
@Setter
@NoArgsConstructor
public class WatchlistDetailsResponse implements PageableItemResource {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private Long id;

    private String name;

    private String description;

    private WatchlistConfigurationResponse configuration;

    private WatchlistFiltersResponse filters;

    private String createdAt;

    private String updatedAt;
}
