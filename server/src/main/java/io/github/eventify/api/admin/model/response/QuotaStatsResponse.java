package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Quota statistics across all users.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "User event quota statistics")
public class QuotaStatsResponse {

    @Schema(
        description = "Number of users with event_count >= 800 and < 1000 (near limit)",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long usersNearLimit;

    @Schema(
        description = "Number of users with event_count >= 1000 (at or over limit)",
        example = "3",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long usersAtLimit;

    @Schema(
        description = "Average quota utilization as percentage (avg(event_count)/1000*100)",
        example = "42.5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double averageUtilization;
}
