package io.github.eventify.api.monitor.model.response;

import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.Timeline;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for a channel with its timeline.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Channel with its timeline")
public class ChannelResponse {

    @Schema(
        description = "Channel identifier",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long channelId;

    @Schema(
        description = "Channel name",
        example = "production-api",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String channelName;

    @Schema(
        description = "Channel status",
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ChannelStatus status;

    @Schema(
        description = "Current severity (most recent event)",
        example = "OK",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Severity currentSeverity;

    @Schema(
        description = "Channel timeline",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Timeline timeline;
}
