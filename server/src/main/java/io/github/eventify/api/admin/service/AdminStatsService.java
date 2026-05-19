package io.github.eventify.api.admin.service;

import io.github.eventify.api.admin.model.AdminCounts;
import io.github.eventify.api.admin.model.AdminEventVolume;
import io.github.eventify.api.admin.model.AdminGrowth;
import io.github.eventify.api.admin.model.DailyVolumeData;
import io.github.eventify.api.admin.model.StorageStats;
import io.github.eventify.api.admin.model.mapper.AdminStatsMapper;
import io.github.eventify.api.admin.model.projection.DailyGrowthData;
import io.github.eventify.api.admin.model.response.AdminStatsResponse;
import io.github.eventify.api.admin.model.response.GrowthDataPoint;
import io.github.eventify.api.admin.repository.AdminStorageRepository;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.util.TimeProvider;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aggregates platform-wide admin statistics (users, orgs, channels, growth). */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;

    private final ChannelRepository channelRepository;

    private final EventRepository eventRepository;

    private final AdminStorageRepository adminStorageRepository;

    private final AdminStatsMapper adminStatsMapper;

    /** Returns counts for organizations, users and channels. */
    @Cacheable("adminCounts")
    @Transactional(readOnly = true)
    public AdminCounts getAdminCounts() {
        return AdminCounts.builder()
            .totalOrganizations(organizationRepository.count())
            .totalUsers(userRepository.count())
            .activeUsers(userRepository.countByValidatedTrue())
            .totalChannels(channelRepository.count())
            .activeChannels(channelRepository.countByStatus(ChannelStatus.ACTIVE))
            .pausedChannels(channelRepository.countByStatus(ChannelStatus.PAUSED))
            .staleChannels(channelRepository.countByIsStaleTrue())
            .pendingDeletionChannels(channelRepository.countByStatus(ChannelStatus.PENDING_DELETION))
            .build();
    }

    /** Returns growth data points for the given time window. */
    @Cacheable(
        value = "adminGrowth",
        key = "#days"
    )
    @Transactional(readOnly = true)
    public AdminGrowth getAdminGrowth(final int days) {
        final List<GrowthDataPoint> growthData = calculateGrowthData(days);
        return AdminGrowth.builder()
            .growthData(growthData)
            .bestGrowthDayUsers(findBestDay(growthData, GrowthDataPoint::getNewUsers))
            .bestGrowthDayOrganizations(findBestDay(growthData, GrowthDataPoint::getNewOrganizations))
            .bestGrowthDayEvents(findBestDay(growthData, GrowthDataPoint::getNewEvents))
            .build();
    }

    /** Returns daily event volume for the given time window. */
    @Cacheable(
        value = "adminEventVolume",
        key = "#days"
    )
    @Transactional(readOnly = true)
    public AdminEventVolume getEventVolume(final int days) {
        final OffsetDateTime since = TimeProvider.startOfDayUtc(LocalDate.now().minusDays(days));
        final long totalEvents = eventRepository.countByTimestampAfter(since);

        final LocalDate today = LocalDate.now();
        final LocalDate startDate = today.minusDays(days);
        final OffsetDateTime start = TimeProvider.startOfDayUtc(startDate);
        final Map<LocalDate, DailyGrowthData> eventCounts = toDateMap(eventRepository.findDailyEventCounts(start));

        final List<DailyVolumeData> dailyVolume = startDate.datesUntil(today.plusDays(1))
            .map(date -> {
                final DailyGrowthData data = eventCounts.get(date);
                final long count = data != null ? data.getNew() : 0L;
                return DailyVolumeData.builder()
                    .date(date)
                    .eventCount(count)
                    .build();
            })
            .toList();

        return AdminEventVolume.builder()
            .totalEvents(totalEvents)
            .dailyVolume(dailyVolume)
            .build();
    }

    /** Aggregates platform statistics for the given time window. */
    @Cacheable(
        value = "adminStats",
        key = "#days"
    )
    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStats(final int days) {
        final AdminCounts counts = getAdminCounts();
        final AdminGrowth growth = getAdminGrowth(days);

        final OffsetDateTime since = TimeProvider.startOfDayUtc(LocalDate.now().minusDays(days));
        final long totalEventsInPeriod = eventRepository.countByTimestampAfter(since);

        return AdminStatsResponse.builder()
            .totalOrganizations(counts.getTotalOrganizations())
            .totalUsers(counts.getTotalUsers())
            .activeUsers(counts.getActiveUsers())
            .totalChannels(counts.getTotalChannels())
            .activeChannels(counts.getActiveChannels())
            .pausedChannels(counts.getPausedChannels())
            .staleChannels(counts.getStaleChannels())
            .pendingDeletionChannels(counts.getPendingDeletionChannels())
            .totalEventsInPeriod(totalEventsInPeriod)
            .growthData(growth.getGrowthData())
            .bestGrowthDayUsers(growth.getBestGrowthDayUsers())
            .bestGrowthDayOrganizations(growth.getBestGrowthDayOrganizations())
            .bestGrowthDayEvents(growth.getBestGrowthDayEvents())
            .build();
    }

    /** Returns storage statistics for all tracked database tables. */
    @Cacheable("adminStorage")
    @Transactional(readOnly = true)
    public List<StorageStats> getStorageStats() {
        return adminStatsMapper.toStorageStatsList(adminStorageRepository.findStorageSizes());
    }

    private GrowthDataPoint findBestDay(final List<GrowthDataPoint> growthData, final ToIntFunction<GrowthDataPoint> metric) {
        return growthData.stream()
            .filter(p -> metric.applyAsInt(p) > 0)
            .max(Comparator.comparingInt(metric))
            .orElse(null);
    }

    private List<GrowthDataPoint> calculateGrowthData(final int days) {
        final LocalDate today = LocalDate.now();
        final LocalDate startDate = today.minusDays(days);

        final OffsetDateTime start = TimeProvider.startOfDayUtc(startDate);
        final OffsetDateTime end = TimeProvider.startOfDayUtc(today);

        final Map<LocalDate, DailyGrowthData> userCounts = toDateMap(userRepository.findDailyGrowthCounts(start, end));
        final Map<LocalDate, DailyGrowthData> orgCounts = toDateMap(organizationRepository.findDailyGrowthCounts(start, end));
        final Map<LocalDate, DailyGrowthData> eventCounts = toDateMap(eventRepository.findDailyEventCounts(start));

        final List<GrowthDataPoint> dataPoints = startDate.datesUntil(today.plusDays(1))
            .map(
                date -> GrowthDataPoint.builder()
                    .date(date)
                    .totalUsers(extractTotal(userCounts.get(date)))
                    .totalOrganizations(extractTotal(orgCounts.get(date)))
                    .newUsers(extractNew(userCounts.get(date)))
                    .newOrganizations(extractNew(orgCounts.get(date)))
                    .newEvents(extractNew(eventCounts.get(date)))
                    .build()
            )
            .toList();

        applyGrowthPercentages(dataPoints);
        return dataPoints;
    }

    private Map<LocalDate, DailyGrowthData> toDateMap(final List<DailyGrowthData> data) {
        return data.stream().collect(Collectors.toMap(DailyGrowthData::getDate, Function.identity()));
    }

    private int extractTotal(final DailyGrowthData data) {
        return data != null ? data.getTotal().intValue() : 0;
    }

    private int extractNew(final DailyGrowthData data) {
        return data != null ? data.getNew().intValue() : 0;
    }

    private void applyGrowthPercentages(final List<GrowthDataPoint> dataPoints) {
        for (int i = 0; i < dataPoints.size(); i++) {
            final GrowthDataPoint current = dataPoints.get(i);
            final GrowthDataPoint previous = i > 0 ? dataPoints.get(i - 1) : null;

            current.setNewUsersGrowthPercentage(growthPercentage(previous, current.getTotalUsers(), GrowthDataPoint::getTotalUsers));
            current.setNewOrganizationsGrowthPercentage(
                growthPercentage(previous, current.getTotalOrganizations(), GrowthDataPoint::getTotalOrganizations)
            );
            current.setNewEventsGrowthPercentage(growthPercentage(previous, current.getNewEvents(), GrowthDataPoint::getNewEvents));
        }
    }

    private Double growthPercentage(final GrowthDataPoint previous, final int currentValue, final ToIntFunction<GrowthDataPoint> metric) {
        if (previous == null) {
            return null;
        }
        final int previousValue = metric.applyAsInt(previous);
        return previousValue != 0
            ? ((currentValue - previousValue) / (double) previousValue) * 100.0
            : 0.0;
    }
}
