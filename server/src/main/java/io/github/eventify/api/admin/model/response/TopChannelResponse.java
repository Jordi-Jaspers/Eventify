package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Top channel entry by event volume.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Top channel by event volume")
public class TopChannelResponse {

    @Schema(
        description = "Channel ID",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long channelId;

    @Schema(
        description = "Channel name",
        example = "production-alerts",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String channelName;

    @Schema(
        description = "Owner display name",
        example = "John Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String ownerName;

    @Schema(
        description = "Total event count in the period",
        example = "5000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long eventCount;

    @Schema(
        description = "Percentage of total events in the period",
        example = "25.0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double percentage;
}
