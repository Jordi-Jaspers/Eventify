package io.github.eventify.api.monitor.model.response;

import io.github.eventify.api.monitor.model.MonitorFilters;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Response containing watchlist timeline data.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Monitor response with dashboard and channel timelines")
public class MonitorResponse {

    @Schema(
        description = "Watchlist identifier",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long watchlistId;

    @Schema(
        description = "Watchlist name",
        example = "Production Services",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String watchlistName;

    @Schema(
        description = "Start of time range",
        example = "2026-01-23T10:00:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime rangeStart;

    @Schema(
        description = "End of time range",
        example = "2026-01-24T10:00:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime rangeEnd;

    @Schema(
        description = "Whether this is live mode (extends to now)",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean live;

    @Schema(
        description = "Watchlist configuration with dashboard, channels and groups",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private DashboardResponse dashboard;

    @Schema(
        description = "Applied filter settings",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private MonitorFilters filters;
}
