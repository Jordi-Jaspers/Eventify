package io.github.eventify.api.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Domain object holding aggregated event statistics. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStats {

    private List<DailyIngestion> dailyIngestion;

    private List<TopChannelInfo> topChannels;

    private SeverityBreakdown severityBreakdown;

    private QuotaStats quotaStats;
}
