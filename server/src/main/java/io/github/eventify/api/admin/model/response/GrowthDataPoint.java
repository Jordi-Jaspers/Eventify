package io.github.eventify.api.admin.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * Response object representing a single growth data point.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GrowthDataPoint {

    private LocalDate date;

    // Cumulative Change (daily)
    private int totalUsers;
    private int totalOrganizations;

    // Relative change (daily)
    private int newUsers;
    private int newOrganizations;

    // Growth percentages (daily)
    private Double newUsersGrowthPercentage;
    private Double newOrganizationsGrowthPercentage;
}
