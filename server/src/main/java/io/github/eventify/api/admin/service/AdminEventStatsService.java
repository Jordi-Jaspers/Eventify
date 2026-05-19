package io.github.eventify.api.admin.service;

import io.github.eventify.api.admin.model.DailyIngestion;
import io.github.eventify.api.admin.model.EventStats;
import io.github.eventify.api.admin.model.QuotaStats;
import io.github.eventify.api.admin.model.SeverityBreakdown;
import io.github.eventify.api.admin.model.TopChannelInfo;
import io.github.eventify.api.admin.model.projection.DailyEventIngestion;
import io.github.eventify.api.admin.model.projection.TopChannelData;
import io.github.eventify.api.admin.repository.EventTimelineRepository;
import io.github.eventify.api.quota.repository.UserEventQuotaRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aggregates event analytics from TimescaleDB continuous aggregate and quota data. */
@Service
@RequiredArgsConstructor
public class AdminEventStatsService {

    private static final int MONTHLY_LIMIT = 1000;
    private static final int NEAR_LIMIT_THRESHOLD = 800;
    private static final int TOP_CHANNELS_LIMIT = 10;

    private static final String SEVERITY_CRITICAL = "CRITICAL";
    private static final String SEVERITY_WARNING = "WARNING";
    private static final String SEVERITY_OK = "OK";

    private final EventTimelineRepository eventTimelineRepository;
    private final UserEventQuotaRepository userEventQuotaRepository;

    /** Builds event statistics for the given number of days. */
    @Transactional(readOnly = true)
    public EventStats getEventStats(final int days) {
        final OffsetDateTime from = startOfDayUtc(LocalDate.now().minusDays(days));
        final OffsetDateTime to = startOfDayUtc(LocalDate.now().plusDays(1));

        return EventStats.builder()
            .dailyIngestion(buildDailyIngestion(from, to))
            .topChannels(buildTopChannels(from, to))
            .severityBreakdown(buildSeverityBreakdown(from, to))
            .quotaStats(buildQuotaStats())
            .build();
    }

    private List<DailyIngestion> buildDailyIngestion(final OffsetDateTime from, final OffsetDateTime to) {
        return eventTimelineRepository.findDailyIngestion(from, to).stream()
            .map(AdminEventStatsService::toDailyIngestion)
            .toList();
    }

    private List<TopChannelInfo> buildTopChannels(final OffsetDateTime from, final OffsetDateTime to) {
        return eventTimelineRepository.findTopChannels(from, to, TOP_CHANNELS_LIMIT).stream()
            .map(AdminEventStatsService::toTopChannelInfo)
            .toList();
    }

    private SeverityBreakdown buildSeverityBreakdown(final OffsetDateTime from, final OffsetDateTime to) {
        return SeverityBreakdown.builder()
            .critical(eventTimelineRepository.countBySeverity(from, to, SEVERITY_CRITICAL))
            .warning(eventTimelineRepository.countBySeverity(from, to, SEVERITY_WARNING))
            .ok(eventTimelineRepository.countBySeverity(from, to, SEVERITY_OK))
            .build();
    }

    private QuotaStats buildQuotaStats() {
        return QuotaStats.builder()
            .usersNearLimit(userEventQuotaRepository.countUsersNearLimit(NEAR_LIMIT_THRESHOLD, MONTHLY_LIMIT))
            .usersAtLimit(userEventQuotaRepository.countUsersAtLimit(MONTHLY_LIMIT))
            .averageUtilization(userEventQuotaRepository.calculateAverageUtilization(MONTHLY_LIMIT))
            .build();
    }

    private static OffsetDateTime startOfDayUtc(final LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
    }

    private static DailyIngestion toDailyIngestion(final DailyEventIngestion d) {
        return DailyIngestion.builder()
            .date(d.getDate())
            .eventCount(d.getEventCount())
            .build();
    }

    private static TopChannelInfo toTopChannelInfo(final TopChannelData c) {
        return TopChannelInfo.builder()
            .channelId(c.getChannelId())
            .channelName(c.getChannelName())
            .ownerName(c.getOwnerName())
            .eventCount(c.getEventCount())
            .percentage(c.getPercentage())
            .build();
    }
}
