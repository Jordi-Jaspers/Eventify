package io.github.eventify.api.monitor.model.response;

import io.github.eventify.api.monitor.model.Timeline;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a channel group with its consolidated timeline and member channels.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Channel group with consolidated timeline")
public class ChannelGroupResponse {

    @Schema(
        description = "Group identifier",
        example = "550e8400-e29b-41d4-a716-446655440000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID id;

    @Schema(
        description = "Group name",
        example = "API Services",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "Consolidated timeline for the group (worst severity across all members)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Timeline timeline;

    @Schema(
        description = "Member channels with individual timelines",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<ChannelResponse> channels;
}
