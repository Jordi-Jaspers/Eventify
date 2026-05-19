package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/** Growth data point for a single day. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Growth data point for a single day")
public class GrowthDataPoint {

    @Schema(
        description = "Date of the data point",
        example = "2026-01-15",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private LocalDate date;

    @Schema(
        description = "Cumulative total number of users",
        example = "1234",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private int totalUsers;

    @Schema(
        description = "Cumulative total number of organizations",
        example = "42",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private int totalOrganizations;

    @Schema(
        description = "Number of new users added on this date",
        example = "15",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private int newUsers;

    @Schema(
        description = "Number of new organizations added on this date",
        example = "3",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private int newOrganizations;

    @Schema(
        description = "Percentage growth in new users compared to previous period",
        example = "5.5",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Double newUsersGrowthPercentage;

    @Schema(
        description = "Percentage growth in new organizations compared to previous period",
        example = "10.0",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Double newOrganizationsGrowthPercentage;

    @Schema(
        description = "Number of new events ingested on this date",
        example = "1500",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private int newEvents;

    @Schema(
        description = "Percentage growth in new events compared to previous period",
        example = "12.5",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Double newEventsGrowthPercentage;
}
