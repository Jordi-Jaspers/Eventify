package io.github.eventify.api.organization.service;

import io.github.eventify.api.admin.stats.repository.EventTimelineRepository;
import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.repository.ApiKeyAuditRepository;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.api.organization.model.OrgApiKeyStatistics;
import io.github.eventify.api.organization.model.OrgErrorRateBucket;
import io.github.eventify.api.organization.model.OrgSummary;
import io.github.eventify.api.organization.model.OrgTimeline;
import io.github.eventify.api.organization.model.OrgTimelineBucketData;
import io.github.eventify.api.organization.model.OrgTopKey;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for org-scoped event statistics. */
@Service
@RequiredArgsConstructor
public class OrgStatsService {

    private static final int HOURLY_BUCKET_MAX_DAYS = 7;
    private static final int SIX_HOUR_BUCKET_MAX_DAYS = 30;

    private final EventTimelineRepository eventTimelineRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyAuditRepository apiKeyAuditRepository;

    /** Returns org event timeline for the given org and time window. */
    @Cacheable(
        value = "orgTimeline",
        key = "#orgId + '-' + #days"
    )
    @Transactional(readOnly = true)
    public OrgTimeline getTimeline(final Long orgId, final int days) {
        return fetchTimeline(orgId, days);
    }

    /** Returns org event summary for the given org and time window. */
    @Cacheable(
        value = "orgSummary",
        key = "#orgId + '-' + #days"
    )
    @Transactional(readOnly = true)
    public OrgSummary getSummary(final Long orgId, final int days) {
        final OrgTimeline orgTimeline = fetchTimeline(orgId, days);
        final List<OrgTimelineBucketData> timeline = orgTimeline.getTimeline();
        final List<OrgErrorRateBucket> errorTimeline = orgTimeline.getErrorTimeline();
        final long totalChannels = eventTimelineRepository.countOrgChannels(orgId);

        final long totalEvents = timeline.stream().mapToLong(OrgTimelineBucketData::getEventCount).sum();
        final long avgDailyVolume = days > 0 ? totalEvents / days : 0L;

        final double avgErrorRate = errorTimeline.isEmpty() ? 0.0
            : errorTimeline.stream().mapToDouble(OrgErrorRateBucket::getErrorRate).average().orElse(0.0);
        final double currentErrorRate = totalEvents > 0 ? avgErrorRate : 0.0;

        return new OrgSummary()
            .setTotalEvents(totalEvents)
            .setAvgDailyVolume(avgDailyVolume)
            .setCurrentErrorRate(currentErrorRate)
            .setTotalChannels(totalChannels);
    }

    /** Returns org API key statistics. */
    @Cacheable(
        value = "orgApiKeyStats",
        key = "#orgId"
    )
    @Transactional(readOnly = true)
    public OrgApiKeyStatistics getApiKeyStats(final Long orgId) {
        return buildApiKeyStats(orgId);
    }

    private OrgTimeline fetchTimeline(final Long orgId, final int days) {
        final LocalDateTime since = LocalDateTime.now().minusDays(days);
        final String interval = resolveInterval(days);

        final List<OrgTimelineBucketData> timeline = eventTimelineRepository.findOrgTimeline(orgId, since, interval)
            .stream()
            .map(p -> new OrgTimelineBucketData(p.getBucket(), p.getEventCount()))
            .toList();

        final List<OrgErrorRateBucket> errorTimeline = eventTimelineRepository.findOrgErrorTimeline(orgId, since, interval)
            .stream()
            .map(p -> new OrgErrorRateBucket(p.getBucket(), p.getErrorRate()))
            .toList();

        return new OrgTimeline()
            .setTimeline(timeline)
            .setErrorTimeline(errorTimeline);
    }

    private String resolveInterval(final int days) {
        if (days <= HOURLY_BUCKET_MAX_DAYS) {
            return "1 hour";
        }
        return days <= SIX_HOUR_BUCKET_MAX_DAYS ? "6 hours" : "1 day";
    }

    private OrgApiKeyStatistics buildApiKeyStats(final Long orgId) {
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final OffsetDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        final OffsetDateTime endOfMonth = startOfMonth.plusMonths(1);

        final long neverUsed = apiKeyRepository.countByOrganizationIdAndLastUsedAtIsNull(orgId);
        final long expiringThisMonth = apiKeyRepository.countByOrganizationIdAndExpiresAtBetween(orgId, startOfMonth, endOfMonth);
        final long revokedThisMonth = apiKeyAuditRepository.countByOrganizationIdAndRevokedAtAfter(orgId, startOfMonth);

        final List<OrgTopKey> topKeys = apiKeyRepository
            .findTopByOrganizationIdOrderByTotalRequestsDesc(orgId, PageRequest.of(0, 5))
            .stream()
            .map(this::toOrgTopKey)
            .toList();

        return new OrgApiKeyStatistics()
            .setNeverUsed(neverUsed)
            .setExpiringThisMonth(expiringThisMonth)
            .setRevokedThisMonth(revokedThisMonth)
            .setTopKeys(topKeys);
    }

    private OrgTopKey toOrgTopKey(final ApiKey key) {
        return new OrgTopKey()
            .setName(key.getName())
            .setSuffix(key.getSuffix())
            .setTotalRequests(key.getTotalRequests());
    }
}
