package io.github.eventify.api.admin.service;

import io.github.eventify.api.admin.model.projection.DailyGrowthData;
import io.github.eventify.api.admin.model.response.AdminStatsResponse;
import io.github.eventify.api.admin.model.response.GrowthDataPoint;
import io.github.eventify.api.admin.model.response.TableSizeEntry;
import io.github.eventify.api.admin.repository.AdminStorageRepository;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for admin dashboard statistics.
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;

    private final ChannelRepository channelRepository;

    private final EventRepository eventRepository;

    private final AdminStorageRepository adminStorageRepository;

    /** Aggregates platform statistics for the given time window. */
    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStats(final int days) {
        final long totalOrganizations = organizationRepository.count();
        final long totalUsers = userRepository.count();
        final long activeUsers = userRepository.countByValidatedTrue();

        final long totalChannels = channelRepository.count();
        final long activeChannels = channelRepository.countByStatus(ChannelStatus.ACTIVE);
        final long pausedChannels = channelRepository.countByStatus(ChannelStatus.PAUSED);
        final long staleChannels = channelRepository.countByIsStaleTrue();
        final long pendingDeletionChannels = channelRepository.countByStatus(ChannelStatus.PENDING_DELETION);

        final OffsetDateTime since = LocalDate.now().minusDays(days).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        final long totalEventsInPeriod = eventRepository.countByTimestampAfter(since);

        final List<GrowthDataPoint> growthData = calculateGrowthData(days);

        return AdminStatsResponse.builder()
            .totalOrganizations(totalOrganizations)
            .totalUsers(totalUsers)
            .activeUsers(activeUsers)
            .totalChannels(totalChannels)
            .activeChannels(activeChannels)
            .pausedChannels(pausedChannels)
            .staleChannels(staleChannels)
            .pendingDeletionChannels(pendingDeletionChannels)
            .totalEventsInPeriod(totalEventsInPeriod)
            .growthData(growthData)
            .bestGrowthDayUsers(findBestDay(growthData, GrowthDataPoint::getNewUsers))
            .bestGrowthDayOrganizations(findBestDay(growthData, GrowthDataPoint::getNewOrganizations))
            .bestGrowthDayEvents(findBestDay(growthData, GrowthDataPoint::getNewEvents))
            .build();
    }

    @Transactional(readOnly = true)
    public List<TableSizeEntry> getStorageStats() {
        return adminStorageRepository.getStorageStats();
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

        final OffsetDateTime start = startDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        final OffsetDateTime end = today.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();

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
        IntStream.range(0, dataPoints.size())
            .forEach(i -> {
                final GrowthDataPoint currentDay = dataPoints.get(i);
                final GrowthDataPoint previousDay = i > 0 ? dataPoints.get(i - 1) : null;

                currentDay.setNewUsersGrowthPercentage(
                    growthPercentage(previousDay, previousDay != null ? previousDay.getTotalUsers() : 0, currentDay.getTotalUsers())
                );
                currentDay.setNewOrganizationsGrowthPercentage(
                    growthPercentage(
                        previousDay,
                        previousDay != null ? previousDay.getTotalOrganizations() : 0,
                        currentDay.getTotalOrganizations()
                    )
                );
                currentDay.setNewEventsGrowthPercentage(
                    growthPercentage(previousDay, previousDay != null ? previousDay.getNewEvents() : 0, currentDay.getNewEvents())
                );
            });
    }

    private Double growthPercentage(final GrowthDataPoint previousDay, final int previousValue, final int currentValue) {
        return previousDay != null ? calculateGrowthPercentage(previousValue, currentValue) : null;
    }

    private double calculateGrowthPercentage(final int previousValue, final int currentValue) {
        return previousValue != 0
            ? ((currentValue - previousValue) / (double) previousValue) * 100.0
            : 0;
    }
}
