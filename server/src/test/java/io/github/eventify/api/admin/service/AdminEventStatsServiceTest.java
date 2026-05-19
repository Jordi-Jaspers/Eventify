package io.github.eventify.api.admin.service;

import io.github.eventify.api.admin.model.EventStats;
import io.github.eventify.api.admin.model.projection.DailyEventIngestion;
import io.github.eventify.api.admin.model.projection.TopChannelData;
import io.github.eventify.api.admin.repository.EventTimelineRepository;
import io.github.eventify.api.quota.repository.UserEventQuotaRepository;
import io.github.eventify.support.UnitTest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - Admin Event Stats Service")
public class AdminEventStatsServiceTest extends UnitTest {

    private AdminEventStatsService adminEventStatsService;

    @Mock
    private EventTimelineRepository eventTimelineRepository;

    @Mock
    private UserEventQuotaRepository userEventQuotaRepository;

    @BeforeEach
    public void setUp() {
        adminEventStatsService = new AdminEventStatsService(
            eventTimelineRepository,
            userEventQuotaRepository
        );
    }

    // ========================= Daily Ingestion Tests =========================

    @Test
    @DisplayName("Should return daily ingestion aggregated from event_timeline_hourly for last N days")
    public void shouldReturnDailyIngestionAggregatedFromEventTimelineHourly() {
        // Given: Event timeline data for the last 30 days
        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        final List<DailyEventIngestion> ingestionData = List.of(
            aDailyEventIngestion(yesterday, 150L),
            aDailyEventIngestion(today, 200L)
        );
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(ingestionData);
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersNearLimit(anyInt(), anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersAtLimit(anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.calculateAverageUtilization(anyInt())).thenReturn(0.0);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: Daily ingestion should contain the aggregated data
        assertThat(stats.getDailyIngestion(), is(notNullValue()));
        assertThat(stats.getDailyIngestion(), hasSize(2));
        assertThat(stats.getDailyIngestion().get(0).getDate(), is(equalTo(yesterday)));
        assertThat(stats.getDailyIngestion().get(0).getEventCount(), is(equalTo(150L)));
        assertThat(stats.getDailyIngestion().get(1).getDate(), is(equalTo(today)));
        assertThat(stats.getDailyIngestion().get(1).getEventCount(), is(equalTo(200L)));
    }

    @Test
    @DisplayName("Should return empty dailyIngestion list when no data in period")
    public void shouldReturnEmptyDailyIngestionWhenNoDataInPeriod() {
        // Given: No event timeline data in the period
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersNearLimit(anyInt(), anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersAtLimit(anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.calculateAverageUtilization(anyInt())).thenReturn(0.0);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: Daily ingestion should be empty
        assertThat(stats.getDailyIngestion(), is(notNullValue()));
        assertThat(stats.getDailyIngestion(), is(empty()));
    }

    // ========================= Top Channels Tests =========================

    @Test
    @DisplayName("Should return top 10 channels by event volume ordered by event count descending")
    public void shouldReturnTop10ChannelsByEventVolumeOrderedDescending() {
        // Given: 10 channels with varying event counts
        final List<TopChannelData> topChannels = List.of(
            aTopChannelData(1L, "Channel A", "Owner A", 5000L, 25.0),
            aTopChannelData(2L, "Channel B", "Owner B", 4000L, 20.0),
            aTopChannelData(3L, "Channel C", "Owner C", 3000L, 15.0),
            aTopChannelData(4L, "Channel D", "Owner D", 2000L, 10.0),
            aTopChannelData(5L, "Channel E", "Owner E", 1500L, 7.5),
            aTopChannelData(6L, "Channel F", "Owner F", 1200L, 6.0),
            aTopChannelData(7L, "Channel G", "Owner G", 1000L, 5.0),
            aTopChannelData(8L, "Channel H", "Owner H", 800L, 4.0),
            aTopChannelData(9L, "Channel I", "Owner I", 600L, 3.0),
            aTopChannelData(10L, "Channel J", "Owner J", 400L, 2.0)
        );
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(topChannels);
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersNearLimit(anyInt(), anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersAtLimit(anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.calculateAverageUtilization(anyInt())).thenReturn(0.0);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: Top channels should contain 10 entries
        assertThat(stats.getTopChannels(), is(notNullValue()));
        assertThat(stats.getTopChannels(), hasSize(10));

        // And: First channel should have highest event count
        assertThat(stats.getTopChannels().get(0).getChannelId(), is(equalTo(1L)));
        assertThat(stats.getTopChannels().get(0).getChannelName(), is(equalTo("Channel A")));
        assertThat(stats.getTopChannels().get(0).getOwnerName(), is(equalTo("Owner A")));
        assertThat(stats.getTopChannels().get(0).getEventCount(), is(equalTo(5000L)));
        assertThat(stats.getTopChannels().get(0).getPercentage(), is(equalTo(25.0)));
    }

    @Test
    @DisplayName("Should return empty topChannels list when no data in period")
    public void shouldReturnEmptyTopChannelsWhenNoDataInPeriod() {
        // Given: No channel data in the period
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersNearLimit(anyInt(), anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersAtLimit(anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.calculateAverageUtilization(anyInt())).thenReturn(0.0);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: Top channels should be empty
        assertThat(stats.getTopChannels(), is(notNullValue()));
        assertThat(stats.getTopChannels(), is(empty()));
    }

    @Test
    @DisplayName("Should handle channel with null name gracefully (deleted channel)")
    public void shouldHandleChannelWithNullNameGracefully() {
        // Given: A channel with null name (deleted channel scenario)
        final List<TopChannelData> topChannels = List.of(
            aTopChannelData(99L, null, null, 1000L, 100.0)
        );
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(topChannels);
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersNearLimit(anyInt(), anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersAtLimit(anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.calculateAverageUtilization(anyInt())).thenReturn(0.0);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: Should not throw and should include the channel entry
        assertThat(stats.getTopChannels(), is(notNullValue()));
        assertThat(stats.getTopChannels(), hasSize(1));
        assertThat(stats.getTopChannels().get(0).getChannelId(), is(equalTo(99L)));
        assertThat(stats.getTopChannels().get(0).getEventCount(), is(equalTo(1000L)));
    }

    // ========================= Severity Breakdown Tests =========================

    @Test
    @DisplayName("Should return severity breakdown counting events by last_severity from event_timeline_hourly")
    public void shouldReturnSeverityBreakdownCountingEventsByLastSeverity() {
        // Given: Events with different severities in the period
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenAnswer(invocation -> {
            final String severity = invocation.getArgument(2);
            return switch (severity) {
                case "CRITICAL" -> 50L;
                case "WARNING" -> 120L;
                case "OK" -> 830L;
                default -> 0L;
            };
        });
        when(userEventQuotaRepository.countUsersNearLimit(anyInt(), anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersAtLimit(anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.calculateAverageUtilization(anyInt())).thenReturn(0.0);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: Severity breakdown should reflect the counts
        assertThat(stats.getSeverityBreakdown(), is(notNullValue()));
        assertThat(stats.getSeverityBreakdown().getCritical(), is(equalTo(50L)));
        assertThat(stats.getSeverityBreakdown().getWarning(), is(equalTo(120L)));
        assertThat(stats.getSeverityBreakdown().getOk(), is(equalTo(830L)));
    }

    @Test
    @DisplayName("Should return zeros for severity breakdown when no data in period")
    public void shouldReturnZerosForSeverityBreakdownWhenNoDataInPeriod() {
        // Given: No events in the period
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersNearLimit(anyInt(), anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersAtLimit(anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.calculateAverageUtilization(anyInt())).thenReturn(0.0);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: All severity counts should be 0
        assertThat(stats.getSeverityBreakdown().getCritical(), is(equalTo(0L)));
        assertThat(stats.getSeverityBreakdown().getWarning(), is(equalTo(0L)));
        assertThat(stats.getSeverityBreakdown().getOk(), is(equalTo(0L)));
    }

    // ========================= Quota Stats Tests =========================

    @Test
    @DisplayName("Should return usersNearLimit count for users with event_count >= 800 AND < 1000")
    public void shouldReturnUsersNearLimitCountForUsersWithEventCountBetween800And1000() {
        // Given: 5 users with event_count between 800 and 999
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersNearLimit(800, 1000)).thenReturn(5L);
        when(userEventQuotaRepository.countUsersAtLimit(anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.calculateAverageUtilization(anyInt())).thenReturn(0.0);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: usersNearLimit should be 5
        assertThat(stats.getQuotaStats().getUsersNearLimit(), is(equalTo(5L)));
    }

    @Test
    @DisplayName("Should return usersAtLimit count for users with event_count >= 1000")
    public void shouldReturnUsersAtLimitCountForUsersWithEventCountAtOrAbove1000() {
        // Given: 3 users with event_count >= 1000
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersNearLimit(anyInt(), anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersAtLimit(1000)).thenReturn(3L);
        when(userEventQuotaRepository.calculateAverageUtilization(anyInt())).thenReturn(0.0);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: usersAtLimit should be 3
        assertThat(stats.getQuotaStats().getUsersAtLimit(), is(equalTo(3L)));
    }

    @Test
    @DisplayName("Should return averageUtilization as avg(event_count)/1000*100")
    public void shouldReturnAverageUtilizationAsPercentageOfMonthlyLimit() {
        // Given: Average event count of 500 across all users (50% utilization)
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersNearLimit(anyInt(), anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersAtLimit(anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.calculateAverageUtilization(1000)).thenReturn(50.0);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: averageUtilization should be 50.0
        assertThat(stats.getQuotaStats().getAverageUtilization(), is(equalTo(50.0)));
    }

    @Test
    @DisplayName("Should return zeros for quota stats when no quota data exists")
    public void shouldReturnZerosForQuotaStatsWhenNoQuotaDataExists() {
        // Given: No quota data
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersNearLimit(anyInt(), anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.countUsersAtLimit(anyInt())).thenReturn(0L);
        when(userEventQuotaRepository.calculateAverageUtilization(anyInt())).thenReturn(0.0);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: All quota stats should be 0
        assertThat(stats.getQuotaStats().getUsersNearLimit(), is(equalTo(0L)));
        assertThat(stats.getQuotaStats().getUsersAtLimit(), is(equalTo(0L)));
        assertThat(stats.getQuotaStats().getAverageUtilization(), is(equalTo(0.0)));
    }

    @Test
    @DisplayName("Should return complete response with all sections populated")
    public void shouldReturnCompleteResponseWithAllSectionsPopulated() {
        // Given: Data for all sections
        final List<DailyEventIngestion> ingestionData = List.of(
            aDailyEventIngestion(LocalDate.now().minusDays(1), 300L)
        );
        final List<TopChannelData> topChannels = List.of(
            aTopChannelData(1L, "Top Channel", "Owner", 300L, 100.0)
        );
        when(eventTimelineRepository.findDailyIngestion(any(), any())).thenReturn(ingestionData);
        when(eventTimelineRepository.findTopChannels(any(), any(), anyInt())).thenReturn(topChannels);
        when(eventTimelineRepository.countBySeverity(any(), any(), any())).thenAnswer(invocation -> {
            final String severity = invocation.getArgument(2);
            return "CRITICAL".equals(severity) ? 10L : "WARNING".equals(severity) ? 50L : 240L;
        });
        when(userEventQuotaRepository.countUsersNearLimit(anyInt(), anyInt())).thenReturn(2L);
        when(userEventQuotaRepository.countUsersAtLimit(anyInt())).thenReturn(1L);
        when(userEventQuotaRepository.calculateAverageUtilization(anyInt())).thenReturn(35.5);

        // When: Getting event stats for 30 days
        final EventStats stats = adminEventStatsService.getEventStats(30);

        // Then: All sections should be populated
        assertThat(stats.getDailyIngestion(), hasSize(1));
        assertThat(stats.getTopChannels(), hasSize(1));
        assertThat(stats.getSeverityBreakdown().getCritical(), is(equalTo(10L)));
        assertThat(stats.getSeverityBreakdown().getWarning(), is(equalTo(50L)));
        assertThat(stats.getSeverityBreakdown().getOk(), is(equalTo(240L)));
        assertThat(stats.getQuotaStats().getUsersNearLimit(), is(equalTo(2L)));
        assertThat(stats.getQuotaStats().getUsersAtLimit(), is(equalTo(1L)));
        assertThat(stats.getQuotaStats().getAverageUtilization(), is(equalTo(35.5)));
    }

    // ========================= Factory Methods =========================

    private static DailyEventIngestion aDailyEventIngestion(final LocalDate date, final long eventCount) {
        return new DailyEventIngestion() {

            @Override
            public LocalDate getDate() {
                return date;
            }

            @Override
            public Long getEventCount() {
                return eventCount;
            }
        };
    }

    private static TopChannelData aTopChannelData(final Long channelId, final String channelName,
        final String ownerName, final long eventCount, final double percentage) {
        return new TopChannelData() {

            @Override
            public Long getChannelId() {
                return channelId;
            }

            @Override
            public String getChannelName() {
                return channelName;
            }

            @Override
            public String getOwnerName() {
                return ownerName;
            }

            @Override
            public Long getEventCount() {
                return eventCount;
            }

            @Override
            public Double getPercentage() {
                return percentage;
            }
        };
    }
}
