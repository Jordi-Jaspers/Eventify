package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Response object containing admin dashboard statistics.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AdminStatsResponse {

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
        description = "Number of currently active users",
        example = "789",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long activeUsers;

    @Schema(
        description = "List of growth data points over time",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<GrowthDataPoint> growthData;
}
