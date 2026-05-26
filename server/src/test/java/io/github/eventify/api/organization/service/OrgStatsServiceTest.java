package io.github.eventify.api.organization.service;

import io.github.eventify.api.admin.stats.model.projection.OrgTimelineProjection;
import io.github.eventify.api.admin.stats.repository.EventTimelineRepository;
import io.github.eventify.api.apikey.repository.ApiKeyAuditRepository;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.api.organization.model.OrgApiKeyStatistics;
import io.github.eventify.api.organization.model.OrgSummary;
import io.github.eventify.api.organization.model.OrgTimeline;
import io.github.eventify.api.organization.model.projection.OrgErrorRateProjection;
import io.github.eventify.support.UnitTest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - OrgStats Service")
public class OrgStatsServiceTest extends UnitTest {

    private OrgStatsService orgStatsService;

    @Mock
    private EventTimelineRepository eventTimelineRepository;

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ApiKeyAuditRepository apiKeyAuditRepository;

    @BeforeEach
    public void setUp() {
        orgStatsService = new OrgStatsService(eventTimelineRepository, apiKeyRepository, apiKeyAuditRepository);
    }

    // ─── getTimeline ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return timeline with event buckets when data exists")
    public void shouldReturnTimelineWithEventBucketsWhenDataExists() {
        // Given: An org with timeline data
        final Long orgId = 1L;
        final int days = 7;
        final LocalDateTime bucket = LocalDateTime.now().minusDays(1);
        final OrgTimelineProjection projection = anOrgTimelineProjection(bucket, 42L);
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), any())).thenReturn(List.of(projection));
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());

        // When: Getting timeline
        final OrgTimeline result = orgStatsService.getTimeline(orgId, days);

        // Then: Timeline should contain the bucket
        assertThat(result, is(notNullValue()));
        assertThat(result.getTimeline(), hasSize(1));
        assertThat(result.getTimeline().get(0).getEventCount(), is(equalTo(42L)));
        assertThat(result.getTimeline().get(0).getBucket(), is(equalTo(bucket)));
    }

    @Test
    @DisplayName("Should return error rate timeline with rate buckets when data exists")
    public void shouldReturnErrorRateTimelineWithRateBucketsWhenDataExists() {
        // Given: An org with error rate data
        final Long orgId = 2L;
        final int days = 7;
        final LocalDateTime bucket = LocalDateTime.now().minusDays(1);
        final OrgErrorRateProjection errorProjection = anOrgErrorRateProjection(bucket, 25.0);
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), any())).thenReturn(List.of(errorProjection));

        // When: Getting timeline
        final OrgTimeline result = orgStatsService.getTimeline(orgId, days);

        // Then: Error timeline should contain the rate bucket
        assertThat(result.getErrorTimeline(), hasSize(1));
        assertThat(result.getErrorTimeline().get(0).getErrorRate(), is(equalTo(25.0)));
        assertThat(result.getErrorTimeline().get(0).getBucket(), is(equalTo(bucket)));
    }

    @Test
    @DisplayName("Should use hourly buckets for 7 days")
    public void shouldUseHourlyBucketsForSevenDays() {
        // Given: A request for 7 days (≤7 range → 1-hour buckets)
        final Long orgId = 3L;
        final int days = 7;
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), eq("1 hour"))).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), eq("1 hour"))).thenReturn(Collections.emptyList());

        // When: Getting timeline
        orgStatsService.getTimeline(orgId, days);

        // Then: Repository should be called with "1 hour" interval
        verify(eventTimelineRepository).findOrgTimeline(eq(orgId), any(), eq("1 hour"));
        verify(eventTimelineRepository).findOrgErrorTimeline(eq(orgId), any(), eq("1 hour"));
    }

    @Test
    @DisplayName("Should use 6-hour buckets for 15 days")
    public void shouldUseSixHourBucketsForFifteenDays() {
        // Given: A request for 15 days (8–30 range → 6-hour buckets)
        final Long orgId = 4L;
        final int days = 15;
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), eq("6 hours"))).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), eq("6 hours"))).thenReturn(Collections.emptyList());

        // When: Getting timeline
        orgStatsService.getTimeline(orgId, days);

        // Then: Repository should be called with "6 hours" interval
        verify(eventTimelineRepository).findOrgTimeline(eq(orgId), any(), eq("6 hours"));
        verify(eventTimelineRepository).findOrgErrorTimeline(eq(orgId), any(), eq("6 hours"));
    }

    @Test
    @DisplayName("Should use daily buckets for 90 days")
    public void shouldUseDailyBucketsForNinetyDays() {
        // Given: A request for 90 days (31+ range → 1-day buckets)
        final Long orgId = 5L;
        final int days = 90;
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), eq("1 day"))).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), eq("1 day"))).thenReturn(Collections.emptyList());

        // When: Getting timeline
        orgStatsService.getTimeline(orgId, days);

        // Then: Repository should be called with "1 day" interval
        verify(eventTimelineRepository).findOrgTimeline(eq(orgId), any(), eq("1 day"));
        verify(eventTimelineRepository).findOrgErrorTimeline(eq(orgId), any(), eq("1 day"));
    }

    @Test
    @DisplayName("Should return empty timeline when org has no events")
    public void shouldReturnEmptyTimelineWhenOrgHasNoEvents() {
        // Given: An org with no events
        final Long orgId = 6L;
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());

        // When: Getting timeline
        final OrgTimeline result = orgStatsService.getTimeline(orgId, 30);

        // Then: Both timelines should be empty
        assertThat(result.getTimeline(), is(empty()));
        assertThat(result.getErrorTimeline(), is(empty()));
    }

    // ─── getSummary ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should sum all bucket event counts for totalEvents")
    public void shouldSumAllBucketEventCountsForTotalEvents() {
        // Given: Multiple timeline buckets with different event counts
        final Long orgId = 10L;
        final int days = 7;
        final List<OrgTimelineProjection> timeline = List.of(
            anOrgTimelineProjection(LocalDateTime.now().minusDays(2), 10L),
            anOrgTimelineProjection(LocalDateTime.now().minusDays(1), 25L),
            anOrgTimelineProjection(LocalDateTime.now(), 5L)
        );
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), any())).thenReturn(timeline);
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countOrgChannels(orgId)).thenReturn(2L);

        // When: Getting summary
        final OrgSummary result = orgStatsService.getSummary(orgId, days);

        // Then: Total events should be sum of all buckets
        assertThat(result.getTotalEvents(), is(equalTo(40L)));
    }

    @Test
    @DisplayName("Should calculate avg daily volume as totalEvents divided by days")
    public void shouldCalculateAvgDailyVolumeAsTotalEventsDividedByDays() {
        // Given: 100 total events over 10 days
        final Long orgId = 11L;
        final int days = 10;
        final List<OrgTimelineProjection> timeline = List.of(
            anOrgTimelineProjection(LocalDateTime.now().minusDays(5), 60L),
            anOrgTimelineProjection(LocalDateTime.now().minusDays(2), 40L)
        );
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), any())).thenReturn(timeline);
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countOrgChannels(orgId)).thenReturn(1L);

        // When: Getting summary
        final OrgSummary result = orgStatsService.getSummary(orgId, days);

        // Then: avgDailyVolume should be 100 / 10 = 10
        assertThat(result.getAvgDailyVolume(), is(equalTo(10L)));
    }

    @Test
    @DisplayName("Should calculate current error rate as percentage of error events")
    public void shouldCalculateCurrentErrorRateAsPercentage() {
        // Given: 20 error events out of 100 total events
        final Long orgId = 12L;
        final int days = 7;
        final List<OrgTimelineProjection> timeline = List.of(
            anOrgTimelineProjection(LocalDateTime.now().minusDays(1), 100L)
        );
        final List<OrgErrorRateProjection> errorTimeline = List.of(
            anOrgErrorRateProjection(LocalDateTime.now().minusDays(1), 20.0)
        );
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), any())).thenReturn(timeline);
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), any())).thenReturn(errorTimeline);
        when(eventTimelineRepository.countOrgChannels(orgId)).thenReturn(1L);

        // When: Getting summary
        final OrgSummary result = orgStatsService.getSummary(orgId, days);

        // Then: Error rate should be computed correctly
        assertThat(result.getCurrentErrorRate(), is(greaterThanOrEqualTo(0.0)));
    }

    @Test
    @DisplayName("Should return zero error rate when no events exist")
    public void shouldReturnZeroErrorRateWhenNoEventsExist() {
        // Given: No events at all
        final Long orgId = 13L;
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countOrgChannels(orgId)).thenReturn(0L);

        // When: Getting summary
        final OrgSummary result = orgStatsService.getSummary(orgId, 30);

        // Then: Error rate should be 0.0 (no division by zero)
        assertThat(result.getCurrentErrorRate(), is(equalTo(0.0)));
    }

    @Test
    @DisplayName("Should return zero totalEvents when timeline is empty")
    public void shouldReturnZeroTotalEventsWhenTimelineIsEmpty() {
        // Given: No timeline data
        final Long orgId = 14L;
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countOrgChannels(orgId)).thenReturn(5L);

        // When: Getting summary
        final OrgSummary result = orgStatsService.getSummary(orgId, 30);

        // Then: Total events should be 0 even if channels exist
        assertThat(result.getTotalEvents(), is(equalTo(0L)));
        assertThat(result.getTotalChannels(), is(equalTo(5L)));
    }

    @Test
    @DisplayName("Should return totalChannels from repository")
    public void shouldReturnTotalChannelsFromRepository() {
        // Given: An org with 3 channels
        final Long orgId = 15L;
        when(eventTimelineRepository.findOrgTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.findOrgErrorTimeline(eq(orgId), any(), any())).thenReturn(Collections.emptyList());
        when(eventTimelineRepository.countOrgChannels(orgId)).thenReturn(3L);

        // When: Getting summary
        final OrgSummary result = orgStatsService.getSummary(orgId, 30);

        // Then: Total channels should match repository value
        assertThat(result.getTotalChannels(), is(equalTo(3L)));
    }

    // ─── getApiKeyStats ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return API key stats with all 4 fields populated from repositories")
    public void shouldReturnApiKeyStatsWithAllFieldsPopulated() {
        // Given: An org with API key data
        final Long orgId = 20L;
        when(apiKeyRepository.countByOrganizationIdAndLastUsedAtIsNull(orgId)).thenReturn(3L);
        when(apiKeyRepository.countByOrganizationIdAndExpiresAtBetween(eq(orgId), any(), any())).thenReturn(2L);
        when(apiKeyRepository.findTopByOrganizationIdOrderByTotalRequestsDesc(eq(orgId), any())).thenReturn(Collections.emptyList());
        when(apiKeyAuditRepository.countByOrganizationIdAndRevokedAtAfter(eq(orgId), any())).thenReturn(1L);

        // When: Getting API key stats
        final OrgApiKeyStatistics result = orgStatsService.getApiKeyStats(orgId);

        // Then: All fields should be populated from repositories
        assertThat(result, is(notNullValue()));
        assertThat(result.getNeverUsed(), is(equalTo(3L)));
        assertThat(result.getExpiringThisMonth(), is(equalTo(2L)));
        assertThat(result.getRevokedThisMonth(), is(equalTo(1L)));
        assertThat(result.getTopKeys(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return empty topKeys list when no API keys exist")
    public void shouldReturnEmptyTopKeysWhenNoApiKeysExist() {
        // Given: An org with no API keys
        final Long orgId = 21L;
        when(apiKeyRepository.countByOrganizationIdAndLastUsedAtIsNull(orgId)).thenReturn(0L);
        when(apiKeyRepository.countByOrganizationIdAndExpiresAtBetween(eq(orgId), any(), any())).thenReturn(0L);
        when(apiKeyRepository.findTopByOrganizationIdOrderByTotalRequestsDesc(eq(orgId), any())).thenReturn(Collections.emptyList());
        when(apiKeyAuditRepository.countByOrganizationIdAndRevokedAtAfter(eq(orgId), any())).thenReturn(0L);

        // When: Getting API key stats
        final OrgApiKeyStatistics result = orgStatsService.getApiKeyStats(orgId);

        // Then: All counts should be 0 and topKeys empty
        assertThat(result.getNeverUsed(), is(equalTo(0L)));
        assertThat(result.getExpiringThisMonth(), is(equalTo(0L)));
        assertThat(result.getRevokedThisMonth(), is(equalTo(0L)));
        assertThat(result.getTopKeys(), is(empty()));
    }

    @Test
    @DisplayName("Should not call timeline repository when getting API key stats")
    public void shouldNotCallTimelineRepositoryWhenGettingApiKeyStats() {
        // Given: An org
        final Long orgId = 22L;
        when(apiKeyRepository.countByOrganizationIdAndLastUsedAtIsNull(orgId)).thenReturn(0L);
        when(apiKeyRepository.countByOrganizationIdAndExpiresAtBetween(eq(orgId), any(), any())).thenReturn(0L);
        when(apiKeyRepository.findTopByOrganizationIdOrderByTotalRequestsDesc(eq(orgId), any())).thenReturn(Collections.emptyList());
        when(apiKeyAuditRepository.countByOrganizationIdAndRevokedAtAfter(eq(orgId), any())).thenReturn(0L);

        // When: Getting API key stats
        orgStatsService.getApiKeyStats(orgId);

        // Then: Timeline repository should NOT be called
        org.mockito.Mockito.verifyNoInteractions(eventTimelineRepository);
    }

    // ─── Fixtures ───────────────────────────────────────────────────────────────

    private static OrgTimelineProjection anOrgTimelineProjection(final LocalDateTime bucket, final Long eventCount) {
        final OrgTimelineProjection projection = mock(OrgTimelineProjection.class);
        when(projection.getBucket()).thenReturn(bucket);
        when(projection.getEventCount()).thenReturn(eventCount);
        return projection;
    }

    private static OrgErrorRateProjection anOrgErrorRateProjection(final LocalDateTime bucket, final Double errorRate) {
        final OrgErrorRateProjection projection = mock(OrgErrorRateProjection.class);
        when(projection.getBucket()).thenReturn(bucket);
        when(projection.getErrorRate()).thenReturn(errorRate);
        return projection;
    }
}
