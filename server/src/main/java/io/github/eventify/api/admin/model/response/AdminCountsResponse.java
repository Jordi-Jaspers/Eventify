package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Admin platform counts response (organizations, users, channels). */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Admin platform counts for organizations, users and channels")
public class AdminCountsResponse {

    @Schema(
        description = "Total number of organizations in the system",
        example = "42",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long totalOrganizations;

    @Schema(
        description = "Total number of users in the system",
        example = "1234",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long totalUsers;

    @Schema(
        description = "Number of currently active (validated) users",
        example = "789",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long activeUsers;

    @Schema(
        description = "Total number of channels in the system",
        example = "150",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long totalChannels;

    @Schema(
        description = "Number of active channels",
        example = "100",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long activeChannels;

    @Schema(
        description = "Number of paused channels",
        example = "20",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long pausedChannels;

    @Schema(
        description = "Number of stale channels",
        example = "15",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long staleChannels;

    @Schema(
        description = "Number of channels pending deletion",
        example = "5",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long pendingDeletionChannels;
}
