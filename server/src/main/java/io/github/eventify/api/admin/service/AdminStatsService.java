package io.github.eventify.api.admin.service;

import io.github.eventify.api.admin.model.projection.DailyGrowthData;
import io.github.eventify.api.admin.model.response.AdminStatsResponse;
import io.github.eventify.api.admin.model.response.GrowthDataPoint;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

/**
 * Service for admin dashboard statistics.
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;

    /**
     * Get admin statistics including total counts and growth data.
     *
     * @return AdminStatsResponse containing all statistics
     */
    public AdminStatsResponse getAdminStats() {
        final long totalOrganizations = organizationRepository.count();
        final long totalUsers = userRepository.count();
        final long activeUsers = userRepository.countByValidatedTrue();

        final List<GrowthDataPoint> growthData = calculateGrowthData();
        return AdminStatsResponse.builder()
            .totalOrganizations(totalOrganizations)
            .totalUsers(totalUsers)
            .activeUsers(activeUsers)
            .growthData(growthData)
            .build();
    }

    private List<GrowthDataPoint> calculateGrowthData() {
        final LocalDate today = LocalDate.now();
        final LocalDate startDate = today.minusDays(30);

        final OffsetDateTime start = startDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        final OffsetDateTime end = today.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();

        final Map<LocalDate, DailyGrowthData> userCounts = userRepository.findDailyGrowthCounts(start, end)
            .stream()
            .collect(Collectors.toMap(DailyGrowthData::getDate, Function.identity()));

        final Map<LocalDate, DailyGrowthData> orgCounts = organizationRepository.findDailyGrowthCounts(start, end)
            .stream()
            .collect(Collectors.toMap(DailyGrowthData::getDate, Function.identity()));

        final List<GrowthDataPoint> dataPoints = startDate.datesUntil(today.plusDays(1))
            .map(date -> {
                final DailyGrowthData userCount = userCounts.get(date);
                final DailyGrowthData orgCount = orgCounts.get(date);

                return GrowthDataPoint.builder()
                    .date(date)
                    .totalUsers(userCount != null ? userCount.getTotal().intValue() : 0)
                    .totalOrganizations(orgCount != null ? orgCount.getTotal().intValue() : 0)
                    .newUsers(userCount != null ? userCount.getNew().intValue() : 0)
                    .newOrganizations(orgCount != null ? orgCount.getNew().intValue() : 0)
                    .build();
            })
            .toList();

        applyGrowthPercentages(dataPoints);
        return dataPoints;
    }

    private void applyGrowthPercentages(final List<GrowthDataPoint> dataPoints) {
        IntStream.range(0, dataPoints.size())
            .forEach(i -> {
                final GrowthDataPoint currentDay = dataPoints.get(i);
                final GrowthDataPoint previousDay = i > 0 ? dataPoints.get(i - 1) : null;

                currentDay.setNewUsersGrowthPercentage(
                    previousDay != null
                        ? calculateGrowthPercentage(previousDay.getTotalUsers(), currentDay.getTotalUsers())
                        : null
                );

                currentDay.setNewOrganizationsGrowthPercentage(
                    previousDay != null
                        ? calculateGrowthPercentage(previousDay.getTotalOrganizations(), currentDay.getTotalOrganizations())
                        : null
                );
            });
    }

    private double calculateGrowthPercentage(final int previousValue, final int currentValue) {
        return previousValue != 0
            ? ((currentValue - previousValue) / (double) previousValue) * 100.0
            : 0;
    }
}
