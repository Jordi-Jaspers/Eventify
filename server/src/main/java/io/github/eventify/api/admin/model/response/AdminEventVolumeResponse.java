package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/** Admin event volume response containing daily volume data from the aggregate. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Admin event volume data for the requested time window")
public class AdminEventVolumeResponse {

    @Schema(
        description = "Total number of events in the requested period",
        example = "50000",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long totalEvents;

    @Schema(
        description = "List of daily event volume data points",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<DailyVolumePoint> dailyVolume;
}
