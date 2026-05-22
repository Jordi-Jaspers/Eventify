package io.github.eventify.api.dashboard.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Watchlist health summary response. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Watchlist health summary")
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class WatchlistHealthResponse {

    @Schema(
        description = "Watchlist unique identifier",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "Watchlist name",
        example = "Production Watchlist",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "Worst severity level across all channels",
        example = "WARNING",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String severity;

    @Schema(
        description = "Number of channels currently in alert",
        example = "3",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int channelsInAlert;

    /** Returns the severity level. */
    public String severity() {
        return severity;
    }
}
