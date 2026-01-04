package io.github.eventify.api.admin.model.response;

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

    private Long totalOrganizations;

    private Long totalUsers;

    private Long activeUsers;

    private List<GrowthDataPoint> growthData;
}
