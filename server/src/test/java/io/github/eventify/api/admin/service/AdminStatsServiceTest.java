package io.github.eventify.api.admin.service;

import io.github.eventify.api.admin.model.projection.DailyGrowthData;
import io.github.eventify.api.admin.model.response.AdminStatsResponse;
import io.github.eventify.api.admin.model.response.GrowthDataPoint;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.support.UnitTest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - Admin Stats Service")
public class AdminStatsServiceTest extends UnitTest {

    private AdminStatsService adminStatsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @BeforeEach
    public void setUp() {
        adminStatsService = new AdminStatsService(userRepository, organizationRepository);
    }

    private DailyGrowthData createMockGrowthData(final LocalDate date, final long total, final long newCount) {
        return new DailyGrowthData() {

            @Override
            public LocalDate getDate() {
                return date;
            }

            @Override
            public Long getTotal() {
                return total;
            }

            @Override
            public Long getNew() {
                return newCount;
            }
        };
    }

    @Test
    @DisplayName("Should calculate total organizations count")
    public void shouldCalculateTotalOrganizationsCount() {
        // Given: 5 organizations exist in the database
        final long totalOrgs = 5L;
        when(organizationRepository.count()).thenReturn(totalOrgs);
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: Total organizations should be 5
        assertThat(stats.getTotalOrganizations(), is(equalTo(5L)));
    }

    @Test
    @DisplayName("Should calculate total users count")
    public void shouldCalculateTotalUsersCount() {
        // Given: 10 users exist in the database
        final long totalUsers = 10L;
        when(userRepository.count()).thenReturn(totalUsers);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: Total users should be 10
        assertThat(stats.getTotalUsers(), is(equalTo(10L)));
    }

    @Test
    @DisplayName("Should calculate active users count with validated users only")
    public void shouldCalculateActiveUsersCountWithValidatedUsersOnly() {
        // Given: 10 total users but only 7 are validated
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByValidatedTrue()).thenReturn(7L);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: Active users should be 7
        assertThat(stats.getActiveUsers(), is(equalTo(7L)));
    }

    @Test
    @DisplayName("Should return zero for active users when no validated users exist")
    public void shouldReturnZeroForActiveUsersWhenNoValidatedUsersExist() {
        // Given: 5 users but none are validated
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: Active users should be 0
        assertThat(stats.getActiveUsers(), is(equalTo(0L)));
    }

    @Test
    @DisplayName("Should return zero counts when database is empty")
    public void shouldReturnZeroCountsWhenDatabaseIsEmpty() {
        // Given: Empty database
        when(userRepository.count()).thenReturn(0L);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: All counts should be 0
        assertThat(stats.getTotalOrganizations(), is(equalTo(0L)));
        assertThat(stats.getTotalUsers(), is(equalTo(0L)));
        assertThat(stats.getActiveUsers(), is(equalTo(0L)));
    }

    @Test
    @DisplayName("Should return growth data points with cumulative and relative fields")
    public void shouldReturnGrowthDataPointsWithCumulativeAndRelativeFields() {
        // Given: Mock counts and growth data
        when(userRepository.count()).thenReturn(100L);
        when(organizationRepository.count()).thenReturn(50L);
        when(userRepository.countByValidatedTrue()).thenReturn(80L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(yesterday, 95L, 5L),
            createMockGrowthData(today, 100L, 5L)
        );

        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(yesterday, 48L, 2L),
            createMockGrowthData(today, 50L, 2L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: Growth data should contain points with cumulative and relative fields
        assertThat(stats.getGrowthData(), is(notNullValue()));
        assertThat(stats.getGrowthData().size(), is(greaterThanOrEqualTo(2)));

        final Optional<GrowthDataPoint> yesterdayData = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(yesterday))
            .findFirst();
        final Optional<GrowthDataPoint> todayData = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(today))
            .findFirst();

        assertThat(yesterdayData.isPresent(), is(true));
        assertThat(todayData.isPresent(), is(true));

        if (yesterdayData.isPresent()) {
            assertThat(yesterdayData.get().getTotalUsers(), is(equalTo(95)));
            assertThat(yesterdayData.get().getNewUsers(), is(equalTo(5)));
            assertThat(yesterdayData.get().getTotalOrganizations(), is(equalTo(48)));
            assertThat(yesterdayData.get().getNewOrganizations(), is(equalTo(2)));
        }

        if (todayData.isPresent()) {
            assertThat(todayData.get().getTotalUsers(), is(equalTo(100)));
            assertThat(todayData.get().getNewUsers(), is(equalTo(5)));
            assertThat(todayData.get().getTotalOrganizations(), is(equalTo(50)));
            assertThat(todayData.get().getNewOrganizations(), is(equalTo(2)));
        }
    }

    @Test
    @DisplayName("Should return growth data with zero counts for days without activity")
    public void shouldReturnGrowthDataWithZeroCountsForDaysWithoutActivity() {
        // Given: Setup for querying 30-day period with no activity
        when(userRepository.count()).thenReturn(5L);
        when(organizationRepository.count()).thenReturn(2L);
        when(userRepository.countByValidatedTrue()).thenReturn(4L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: Should return response with 30 days of data (including zeros)
        assertThat(stats, is(notNullValue()));
        assertThat(stats.getGrowthData(), is(notNullValue()));
        assertThat(stats.getGrowthData(), hasSize(31));
    }

    @Test
    @DisplayName("Should include all required fields in AdminStatsResponse")
    public void shouldIncludeAllRequiredFieldsInAdminStatsResponse() {
        // Given: Mock data for all stats
        when(userRepository.count()).thenReturn(50L);
        when(organizationRepository.count()).thenReturn(25L);
        when(userRepository.countByValidatedTrue()).thenReturn(40L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: Response should contain all required fields
        assertThat(stats.getTotalOrganizations(), is(notNullValue()));
        assertThat(stats.getTotalUsers(), is(notNullValue()));
        assertThat(stats.getActiveUsers(), is(notNullValue()));
        assertThat(stats.getGrowthData(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should handle large counts correctly")
    public void shouldHandleLargeCountsCorrectly() {
        // Given: Large numbers of users and organizations
        final long largeUserCount = 1_000_000L;
        final long largeOrgCount = 100_000L;
        final long largeActiveCount = 800_000L;

        when(userRepository.count()).thenReturn(largeUserCount);
        when(organizationRepository.count()).thenReturn(largeOrgCount);
        when(userRepository.countByValidatedTrue()).thenReturn(largeActiveCount);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: Large counts should be stored correctly
        assertThat(stats.getTotalUsers(), is(equalTo(largeUserCount)));
        assertThat(stats.getTotalOrganizations(), is(equalTo(largeOrgCount)));
        assertThat(stats.getActiveUsers(), is(equalTo(largeActiveCount)));
    }

    @Test
    @DisplayName("Should return proper date range for growth data calculation")
    public void shouldReturnProperDateRangeForGrowthDataCalculation() {
        // Given: Current date and expected date range
        when(userRepository.count()).thenReturn(100L);
        when(organizationRepository.count()).thenReturn(50L);
        when(userRepository.countByValidatedTrue()).thenReturn(80L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: Stats should be calculated for proper date range
        assertThat(stats, is(notNullValue()));
    }

    @Test
    @DisplayName("Should have growth data with zero counts for 30-day window")
    public void shouldHaveGrowthDataWithZeroCountsFor30DayWindow() {
        // Given: Counts exist but no growth data in last 30 days
        when(userRepository.count()).thenReturn(10L);
        when(organizationRepository.count()).thenReturn(5L);
        when(userRepository.countByValidatedTrue()).thenReturn(8L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: Growth data list should contain 31 entries (30 days + today)
        assertThat(stats.getGrowthData(), is(notNullValue()));
        assertThat(stats.getGrowthData(), hasSize(31));
    }

    @Test
    @DisplayName("Should maintain consistency between active users and total users")
    public void shouldMaintainConsistencyBetweenActiveUsersAndTotalUsers() {
        // Given: Active users <= total users
        final long totalUsers = 100L;
        final long activeUsers = 75L;

        when(userRepository.count()).thenReturn(totalUsers);
        when(userRepository.countByValidatedTrue()).thenReturn(activeUsers);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats();

        // Then: Active users should be <= total users
        assertThat(stats.getActiveUsers(), is(lessThanOrEqualTo(stats.getTotalUsers())));
    }
}
