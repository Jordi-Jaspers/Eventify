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
 * Response DTO for admin event statistics endpoint.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Admin event statistics response")
public class AdminEventStatsResponse {

    @Schema(
        description = "Daily event ingestion totals for the requested period",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<DailyIngestionResponse> dailyIngestion;

    @Schema(
        description = "Top 10 channels by event volume in the period",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<TopChannelResponse> topChannels;

    @Schema(
        description = "Event severity breakdown for the period",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private SeverityBreakdownResponse severityBreakdown;

    @Schema(
        description = "User event quota statistics",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private QuotaStatsResponse quotaStats;
}
