package io.github.eventify.api.monitor.model.response;

import io.github.eventify.api.channel.model.response.ChannelGroupResponse;
import io.github.eventify.api.channel.model.response.ChannelResponse;
import io.github.eventify.api.monitor.model.Timeline;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for watchlist configuration containing dashboard, channels and groups.
 * Mirrors the structure of WatchlistConfiguration (which implements TimelineSource).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Watchlist configuration with dashboard timeline, channels and groups")
public class DashboardResponse {

    @Schema(
        description = "Consolidated dashboard timeline (worst severity across all channels and groups)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Timeline timeline;

    @Schema(
        description = "Standalone channels (not in any group)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<ChannelResponse> channels;

    @Schema(
        description = "Channel groups with consolidated timelines and member channels",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<ChannelGroupResponse> groups;
}
