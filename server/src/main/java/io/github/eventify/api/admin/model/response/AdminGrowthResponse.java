package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/** Admin growth response containing growth data points and best days. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Admin growth data for the requested time window")
public class AdminGrowthResponse {

    @Schema(
        description = "List of daily growth data points",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<GrowthDataPoint> growthData;

    @Schema(
        description = "Growth data point with the highest new user count in the period",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private GrowthDataPoint bestGrowthDayUsers;

    @Schema(
        description = "Growth data point with the highest new organization count in the period",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private GrowthDataPoint bestGrowthDayOrganizations;

    @Schema(
        description = "Growth data point with the highest new event count in the period",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private GrowthDataPoint bestGrowthDayEvents;
}
