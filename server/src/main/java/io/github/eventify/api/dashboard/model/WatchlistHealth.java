package io.github.eventify.api.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Watchlist health summary. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WatchlistHealth {

    private Long id;
    private String name;
    private String severity;
    private int channelsInAlert;
}
