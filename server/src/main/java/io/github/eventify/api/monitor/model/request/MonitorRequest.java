package io.github.eventify.api.monitor.model.request;

import io.github.eventify.api.monitor.model.MonitorFilters;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for monitor timeline data.
 */
@Data
@NoArgsConstructor
@Schema(description = "Monitor timeline request")
public class MonitorRequest {

    @Schema(
        description = "Watchlist identifier",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long watchlistId;

    @Schema(
        description = "Filter settings including time range (null values use watchlist defaults)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private MonitorFilters filters;
}
