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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - Admin Stats Service")
public class AdminStatsServiceTest extends UnitTest {

    private AdminStatsService adminStatsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AdminStorageRepository adminStorageRepository;

    @BeforeEach
    public void setUp() throws Exception {
        adminStatsService = new AdminStatsService(
            userRepository,
            organizationRepository,
            channelRepository,
            eventRepository,
            adminStorageRepository
        );

        // Default stub for storage stats
        lenient().when(adminStorageRepository.getStorageStats()).thenReturn(
            List.of(
                TableSizeEntry.builder().tableName("event").sizeBytes(1024L).sizeFormatted("1024 bytes").build(),
                TableSizeEntry.builder().tableName("notification").sizeBytes(2048L).sizeFormatted("2 kB").build(),
                TableSizeEntry.builder().tableName("channel").sizeBytes(512L).sizeFormatted("512 bytes").build(),
                TableSizeEntry.builder().tableName("organization").sizeBytes(256L).sizeFormatted("256 bytes").build(),
                TableSizeEntry.builder().tableName("app_user").sizeBytes(4096L).sizeFormatted("4 kB").build()
            )
        );

        // Default lenient stubs so existing tests don't need to set up new repos
        lenient().when(channelRepository.count()).thenReturn(0L);
        lenient().when(channelRepository.countByStatus(ChannelStatus.ACTIVE)).thenReturn(0L);
        lenient().when(channelRepository.countByStatus(ChannelStatus.PAUSED)).thenReturn(0L);
        lenient().when(channelRepository.countByIsStaleTrue()).thenReturn(0L);
        lenient().when(channelRepository.countByStatus(ChannelStatus.PENDING_DELETION)).thenReturn(0L);
        lenient().when(eventRepository.countByTimestampAfter(any())).thenReturn(0L);
        lenient().when(eventRepository.findDailyEventCounts(any())).thenReturn(Collections.emptyList());
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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

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
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Active users should be <= total users
        assertThat(stats.getActiveUsers(), is(lessThanOrEqualTo(stats.getTotalUsers())));
    }

    // ==================== Percentage Growth Tests ====================

    @Test
    @DisplayName("Should calculate positive growth percentage when users increase")
    public void shouldCalculatePositiveGrowthPercentageWhenUsersIncrease() {
        // Given: Yesterday total 100 users, today total 150 users (50% cumulative growth)
        when(userRepository.count()).thenReturn(150L);
        when(organizationRepository.count()).thenReturn(50L);
        when(userRepository.countByValidatedTrue()).thenReturn(80L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(yesterday, 100L, 10L),
            createMockGrowthData(today, 150L, 50L)
        );

        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(yesterday, 48L, 2L),
            createMockGrowthData(today, 50L, 2L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Today's growth percentage should be 50% based on cumulative totals (150-100)/100
        final Optional<GrowthDataPoint> todayData = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(today))
            .findFirst();

        assertThat(todayData.isPresent(), is(true));
        if (todayData.isPresent()) {
            assertThat(todayData.get().getNewUsersGrowthPercentage(), is(notNullValue()));
            assertThat(todayData.get().getNewUsersGrowthPercentage(), is(equalTo(50.0)));
        }
    }

    @Test
    @DisplayName("Should calculate negative growth percentage when users decrease")
    public void shouldCalculateNegativeGrowthPercentageWhenUsersDecrease() {
        // Given: Yesterday total 100 users, today total 75 users (-25% cumulative growth)
        when(userRepository.count()).thenReturn(75L);
        when(organizationRepository.count()).thenReturn(50L);
        when(userRepository.countByValidatedTrue()).thenReturn(60L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(yesterday, 100L, 20L),
            createMockGrowthData(today, 75L, 0L)
        );

        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(yesterday, 48L, 2L),
            createMockGrowthData(today, 50L, 2L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Today's growth percentage should be -25% based on cumulative totals (75-100)/100
        final Optional<GrowthDataPoint> todayData = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(today))
            .findFirst();

        assertThat(todayData.isPresent(), is(true));
        if (todayData.isPresent()) {
            assertThat(todayData.get().getNewUsersGrowthPercentage(), is(notNullValue()));
            assertThat(todayData.get().getNewUsersGrowthPercentage(), is(equalTo(-25.0)));
        }
    }

    @Test
    @DisplayName("Should handle zero growth percentage when cumulative totals are equal")
    public void shouldHandleZeroGrowthPercentageWhenCountsAreEqual() {
        // Given: Yesterday total 100 users, today total 100 users (0% cumulative growth)
        when(userRepository.count()).thenReturn(100L);
        when(organizationRepository.count()).thenReturn(50L);
        when(userRepository.countByValidatedTrue()).thenReturn(80L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(yesterday, 100L, 10L),
            createMockGrowthData(today, 100L, 0L)
        );

        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(yesterday, 50L, 2L),
            createMockGrowthData(today, 50L, 0L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Today's growth percentage should be 0% based on cumulative totals
        final Optional<GrowthDataPoint> todayData = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(today))
            .findFirst();

        assertThat(todayData.isPresent(), is(true));
        if (todayData.isPresent()) {
            assertThat(todayData.get().getNewUsersGrowthPercentage(), is(notNullValue()));
            assertThat(todayData.get().getNewUsersGrowthPercentage(), is(equalTo(0.0)));
        }
    }

    @Test
    @DisplayName("Should handle division by zero gracefully when previous day had zero total users")
    public void shouldHandleDivisionByZeroGracefullyWhenPreviousDayHadZeroNewUsers() {
        // Given: Yesterday total 0 users, today total 5 users (division by zero scenario)
        when(userRepository.count()).thenReturn(5L);
        when(organizationRepository.count()).thenReturn(2L);
        when(userRepository.countByValidatedTrue()).thenReturn(5L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(yesterday, 0L, 0L),
            createMockGrowthData(today, 5L, 5L)
        );

        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(yesterday, 0L, 0L),
            createMockGrowthData(today, 2L, 2L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Today's growth percentage should be 0.0 (graceful handling of division by zero)
        final Optional<GrowthDataPoint> todayData = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(today))
            .findFirst();

        assertThat(todayData.isPresent(), is(true));
        if (todayData.isPresent()) {
            // Division by zero returns 0.0 as per service implementation
            final Double growthPercentage = todayData.get().getNewUsersGrowthPercentage();
            assertThat(growthPercentage, is(equalTo(0.0)));
        }
    }

    @Test
    @DisplayName("Should calculate organizations growth percentage independently from users")
    public void shouldCalculateOrganizationsGrowthPercentageIndependentlyFromUsers() {
        // Given: Organizations with different cumulative growth metrics than users
        when(userRepository.count()).thenReturn(150L);
        when(organizationRepository.count()).thenReturn(52L);
        when(userRepository.countByValidatedTrue()).thenReturn(120L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        // Users: 100 yesterday -> 150 today (50% cumulative growth)
        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(yesterday, 100L, 10L),
            createMockGrowthData(today, 150L, 50L)
        );

        // Organizations: 26 yesterday -> 52 today (100% cumulative growth)
        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(yesterday, 26L, 2L),
            createMockGrowthData(today, 52L, 26L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Both metrics should be calculated independently based on cumulative totals
        final Optional<GrowthDataPoint> todayData = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(today))
            .findFirst();

        assertThat(todayData.isPresent(), is(true));
        if (todayData.isPresent()) {
            assertThat(todayData.get().getNewUsersGrowthPercentage(), is(notNullValue()));
            assertThat(todayData.get().getNewUsersGrowthPercentage(), is(equalTo(50.0)));
            assertThat(todayData.get().getNewOrganizationsGrowthPercentage(), is(notNullValue()));
            assertThat(todayData.get().getNewOrganizationsGrowthPercentage(), is(equalTo(100.0)));
        }
    }

    @Test
    @DisplayName("Should calculate negative organizations growth percentage")
    public void shouldCalculateNegativeOrganizationsGrowthPercentage() {
        // Given: Organizations declining from 100 to 75 (-25% cumulative growth)
        when(userRepository.count()).thenReturn(200L);
        when(organizationRepository.count()).thenReturn(75L);
        when(userRepository.countByValidatedTrue()).thenReturn(150L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        // Users: 100 -> 200 (100% growth)
        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(yesterday, 100L, 5L),
            createMockGrowthData(today, 200L, 100L)
        );

        // Organizations: 100 -> 75 (-25% growth)
        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(yesterday, 100L, 4L),
            createMockGrowthData(today, 75L, 0L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Organizations growth percentage should be -25% based on cumulative totals
        final Optional<GrowthDataPoint> todayData = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(today))
            .findFirst();

        assertThat(todayData.isPresent(), is(true));
        if (todayData.isPresent()) {
            assertThat(todayData.get().getNewOrganizationsGrowthPercentage(), is(notNullValue()));
            assertThat(todayData.get().getNewOrganizationsGrowthPercentage(), is(equalTo(-25.0)));
        }
    }

    @Test
    @DisplayName("Should have no growth percentage for first day (no previous day)")
    public void shouldHaveNoGrowthPercentageForFirstDay() {
        // Given: Only one day of data (first day, no previous day)
        when(userRepository.count()).thenReturn(100L);
        when(organizationRepository.count()).thenReturn(50L);
        when(userRepository.countByValidatedTrue()).thenReturn(80L);

        final LocalDate today = LocalDate.now();

        // Only provide data for today, no yesterday
        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(today, 100L, 10L)
        );

        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(today, 50L, 2L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: First day should have null or 0.0 growth percentage
        final Optional<GrowthDataPoint> todayData = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(today))
            .findFirst();

        assertThat(todayData.isPresent(), is(true));
        if (todayData.isPresent()) {
            final Double userGrowth = todayData.get().getNewUsersGrowthPercentage();
            final Double orgGrowth = todayData.get().getNewOrganizationsGrowthPercentage();
            // Should be null (no previous day) or 0.0 (default)
            assertThat(userGrowth, anyOf(nullValue(), equalTo(0.0)));
            assertThat(orgGrowth, anyOf(nullValue(), equalTo(0.0)));
        }
    }

    @Test
    @DisplayName("Should calculate precise percentage with decimal values")
    public void shouldCalculatePrecisePercentageWithDecimalValues() {
        // Given: Data that results in precise percentage based on cumulative totals
        // Users: 100 -> 150 = 50%, Organizations: 50 -> 80 = 60%
        when(userRepository.count()).thenReturn(150L);
        when(organizationRepository.count()).thenReturn(80L);
        when(userRepository.countByValidatedTrue()).thenReturn(120L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(yesterday, 100L, 10L),
            createMockGrowthData(today, 150L, 50L)
        );

        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(yesterday, 50L, 5L),
            createMockGrowthData(today, 80L, 30L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Percentages should be precise based on cumulative totals
        final Optional<GrowthDataPoint> todayData = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(today))
            .findFirst();

        assertThat(todayData.isPresent(), is(true));
        if (todayData.isPresent()) {
            // Users: (150 - 100) / 100 * 100 = 50%
            assertThat(todayData.get().getNewUsersGrowthPercentage(), is(equalTo(50.0)));
            // Organizations: (80 - 50) / 50 * 100 = 60%
            assertThat(todayData.get().getNewOrganizationsGrowthPercentage(), is(equalTo(60.0)));
        }
    }

    @Test
    @DisplayName("Should handle organizations with zero previous day total gracefully")
    public void shouldHandleOrganizationsWithZeroPreviousDayGrowthGracefully() {
        // Given: Organizations with zero total yesterday, some today (division by zero scenario)
        when(userRepository.count()).thenReturn(200L);
        when(organizationRepository.count()).thenReturn(3L);
        when(userRepository.countByValidatedTrue()).thenReturn(150L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        // Users: 100 -> 200 (100% growth)
        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(yesterday, 100L, 5L),
            createMockGrowthData(today, 200L, 100L)
        );

        // Organizations: 0 yesterday -> 3 today (division by zero)
        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(yesterday, 0L, 0L),
            createMockGrowthData(today, 3L, 3L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Organizations growth should handle zero gracefully (returns 0.0)
        final Optional<GrowthDataPoint> todayData = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(today))
            .findFirst();

        assertThat(todayData.isPresent(), is(true));
        if (todayData.isPresent()) {
            // Division by zero returns 0.0
            final Double orgGrowth = todayData.get().getNewOrganizationsGrowthPercentage();
            assertThat(orgGrowth, is(equalTo(0.0)));
            // Users growth should still calculate correctly
            assertThat(todayData.get().getNewUsersGrowthPercentage(), is(equalTo(100.0)));
        }
    }

    @Test
    @DisplayName("Should calculate multiple days of growth percentages correctly")
    public void shouldCalculateMultipleDaysOfGrowthPercentagesCorrectly() {
        // Given: Data spanning 3 days with varying cumulative growth patterns
        when(userRepository.count()).thenReturn(120L);
        when(organizationRepository.count()).thenReturn(60L);
        when(userRepository.countByValidatedTrue()).thenReturn(100L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);
        final LocalDate twoDaysAgo = today.minusDays(2);

        // Users cumulative: Day 1: 100, Day 2: 150 (50%), Day 3: 120 (-20%)
        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(twoDaysAgo, 100L, 10L),
            createMockGrowthData(yesterday, 150L, 50L),
            createMockGrowthData(today, 120L, 0L)
        );

        // Organizations cumulative: Day 1: 40, Day 2: 60 (50%), Day 3: 60 (0%)
        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(twoDaysAgo, 40L, 2L),
            createMockGrowthData(yesterday, 60L, 20L),
            createMockGrowthData(today, 60L, 0L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: All percentages should be calculated correctly based on cumulative totals
        final Optional<GrowthDataPoint> yesterdayPoint = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(yesterday))
            .findFirst();
        final Optional<GrowthDataPoint> todayPoint = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(today))
            .findFirst();

        assertThat(yesterdayPoint.isPresent(), is(true));
        assertThat(todayPoint.isPresent(), is(true));

        if (yesterdayPoint.isPresent()) {
            // Users: (150 - 100) / 100 * 100 = 50%
            assertThat(yesterdayPoint.get().getNewUsersGrowthPercentage(), is(equalTo(50.0)));
            // Orgs: (60 - 40) / 40 * 100 = 50%
            assertThat(yesterdayPoint.get().getNewOrganizationsGrowthPercentage(), is(equalTo(50.0)));
        }

        if (todayPoint.isPresent()) {
            // Users: (120 - 150) / 150 * 100 = -20%
            assertThat(todayPoint.get().getNewUsersGrowthPercentage(), is(equalTo(-20.0)));
            // Orgs: (60 - 60) / 60 * 100 = 0%
            assertThat(todayPoint.get().getNewOrganizationsGrowthPercentage(), is(equalTo(0.0)));
        }
    }

    @Test
    @DisplayName("Should include percentage fields in all growth data points")
    public void shouldIncludePercentageFieldsInAllGrowthDataPoints() {
        // Given: Data for multiple days
        when(userRepository.count()).thenReturn(100L);
        when(organizationRepository.count()).thenReturn(50L);
        when(userRepository.countByValidatedTrue()).thenReturn(80L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(yesterday, 90L, 5L),
            createMockGrowthData(today, 100L, 10L)
        );

        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(yesterday, 48L, 2L),
            createMockGrowthData(today, 50L, 2L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Every data point should have percentage fields (not null, even if 0 or null)
        for (final GrowthDataPoint point : stats.getGrowthData()) {
            // Percentage fields should either exist and be a number, or be null
            // They should not be missing/uninitialized
            if (point.getNewUsersGrowthPercentage() != null) {
                assertThat(point.getNewUsersGrowthPercentage(), is(instanceOf(Double.class)));
            }
            if (point.getNewOrganizationsGrowthPercentage() != null) {
                assertThat(point.getNewOrganizationsGrowthPercentage(), is(instanceOf(Double.class)));
            }
        }
    }

    // ==================== Days Param Tests ====================

    @Test
    @DisplayName("Should return 7-day growth data when days=7")
    public void shouldReturn7DayGrowthDataWhenDaysIs7() {
        // Given: All repos return empty data
        when(userRepository.count()).thenReturn(10L);
        when(organizationRepository.count()).thenReturn(5L);
        when(userRepository.countByValidatedTrue()).thenReturn(8L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats for 7 days
        final AdminStatsResponse stats = adminStatsService.getAdminStats(7);

        // Then: Growth data should contain 8 entries (7 days + today)
        assertThat(stats.getGrowthData(), is(notNullValue()));
        assertThat(stats.getGrowthData(), hasSize(8));
    }

    @Test
    @DisplayName("Should return 90-day growth data when days=90")
    public void shouldReturn90DayGrowthDataWhenDaysIs90() {
        // Given: All repos return empty data
        when(userRepository.count()).thenReturn(10L);
        when(organizationRepository.count()).thenReturn(5L);
        when(userRepository.countByValidatedTrue()).thenReturn(8L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats for 90 days
        final AdminStatsResponse stats = adminStatsService.getAdminStats(90);

        // Then: Growth data should contain 91 entries (90 days + today)
        assertThat(stats.getGrowthData(), is(notNullValue()));
        assertThat(stats.getGrowthData(), hasSize(91));
    }

    // ==================== Channel Stats Tests ====================

    @Test
    @DisplayName("Should return total channels count")
    public void shouldReturnTotalChannelsCount() {
        // Given: 20 channels exist
        when(userRepository.count()).thenReturn(0L);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(channelRepository.count()).thenReturn(20L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Total channels should be 20
        assertThat(stats.getTotalChannels(), is(equalTo(20L)));
    }

    @Test
    @DisplayName("Should return channel counts by status")
    public void shouldReturnChannelCountsByStatus() {
        // Given: Channels with different statuses
        when(userRepository.count()).thenReturn(0L);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(channelRepository.count()).thenReturn(30L);
        when(channelRepository.countByStatus(ChannelStatus.ACTIVE)).thenReturn(15L);
        when(channelRepository.countByStatus(ChannelStatus.PAUSED)).thenReturn(5L);
        when(channelRepository.countByIsStaleTrue()).thenReturn(7L);
        when(channelRepository.countByStatus(ChannelStatus.PENDING_DELETION)).thenReturn(3L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Each status count should be correct
        assertThat(stats.getActiveChannels(), is(equalTo(15L)));
        assertThat(stats.getPausedChannels(), is(equalTo(5L)));
        assertThat(stats.getStaleChannels(), is(equalTo(7L)));
        assertThat(stats.getPendingDeletionChannels(), is(equalTo(3L)));
    }

    @Test
    @DisplayName("Should return zero channel counts when no channels exist")
    public void shouldReturnZeroChannelCountsWhenNoChannelsExist() {
        // Given: No channels in database
        when(userRepository.count()).thenReturn(0L);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(channelRepository.count()).thenReturn(0L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: All channel counts should be 0
        assertThat(stats.getTotalChannels(), is(equalTo(0L)));
        assertThat(stats.getActiveChannels(), is(equalTo(0L)));
        assertThat(stats.getPausedChannels(), is(equalTo(0L)));
        assertThat(stats.getStaleChannels(), is(equalTo(0L)));
        assertThat(stats.getPendingDeletionChannels(), is(equalTo(0L)));
    }

    // ==================== Event Stats Tests ====================

    @Test
    @DisplayName("Should return total events in period")
    public void shouldReturnTotalEventsInPeriod() {
        // Given: 500 events in the period
        when(userRepository.count()).thenReturn(0L);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(eventRepository.countByTimestampAfter(any())).thenReturn(500L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Total events in period should be 500
        assertThat(stats.getTotalEventsInPeriod(), is(equalTo(500L)));
    }

    @Test
    @DisplayName("Should return zero events in period when no events exist")
    public void shouldReturnZeroEventsInPeriodWhenNoEventsExist() {
        // Given: No events in the period
        when(userRepository.count()).thenReturn(0L);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(eventRepository.countByTimestampAfter(any())).thenReturn(0L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Total events in period should be 0
        assertThat(stats.getTotalEventsInPeriod(), is(equalTo(0L)));
    }

    // ==================== Best Growth Day Tests ====================

    @Test
    @DisplayName("Should return best growth day for users when growth data exists")
    public void shouldReturnBestGrowthDayForUsersWhenGrowthDataExists() {
        // Given: Multiple days of user growth data
        when(userRepository.count()).thenReturn(100L);
        when(organizationRepository.count()).thenReturn(50L);
        when(userRepository.countByValidatedTrue()).thenReturn(80L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);
        final LocalDate twoDaysAgo = today.minusDays(2);

        // Day with most new users is yesterday (50 new)
        final List<DailyGrowthData> userGrowthData = List.of(
            createMockGrowthData(twoDaysAgo, 50L, 5L),
            createMockGrowthData(yesterday, 100L, 50L),
            createMockGrowthData(today, 100L, 0L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(userGrowthData);
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Best growth day for users should be yesterday
        assertThat(stats.getBestGrowthDayUsers(), is(notNullValue()));
        assertThat(stats.getBestGrowthDayUsers().getDate(), is(equalTo(yesterday)));
    }

    @Test
    @DisplayName("Should return null best growth day for users when no growth data exists")
    public void shouldReturnNullBestGrowthDayForUsersWhenNoGrowthDataExists() {
        // Given: No growth data
        when(userRepository.count()).thenReturn(0L);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Best growth day for users should be null
        assertThat(stats.getBestGrowthDayUsers(), is(nullValue()));
    }

    @Test
    @DisplayName("Should return best growth day for organizations when growth data exists")
    public void shouldReturnBestGrowthDayForOrganizationsWhenGrowthDataExists() {
        // Given: Multiple days of org growth data
        when(userRepository.count()).thenReturn(100L);
        when(organizationRepository.count()).thenReturn(50L);
        when(userRepository.countByValidatedTrue()).thenReturn(80L);

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);
        final LocalDate twoDaysAgo = today.minusDays(2);

        // Day with most new orgs is two days ago (10 new)
        final List<DailyGrowthData> orgGrowthData = List.of(
            createMockGrowthData(twoDaysAgo, 30L, 10L),
            createMockGrowthData(yesterday, 33L, 3L),
            createMockGrowthData(today, 35L, 2L)
        );

        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(orgGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Best growth day for organizations should be two days ago
        assertThat(stats.getBestGrowthDayOrganizations(), is(notNullValue()));
        assertThat(stats.getBestGrowthDayOrganizations().getDate(), is(equalTo(twoDaysAgo)));
    }

    @Test
    @DisplayName("Should return null best growth day for organizations when no growth data exists")
    public void shouldReturnNullBestGrowthDayForOrganizationsWhenNoGrowthDataExists() {
        // Given: No growth data
        when(userRepository.count()).thenReturn(0L);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Best growth day for organizations should be null
        assertThat(stats.getBestGrowthDayOrganizations(), is(nullValue()));
    }

    @Test
    @DisplayName("Should return best growth day for events when event data exists")
    public void shouldReturnBestGrowthDayForEventsWhenEventDataExists() {
        // Given: Multiple days of event growth data
        when(userRepository.count()).thenReturn(100L);
        when(organizationRepository.count()).thenReturn(50L);
        when(userRepository.countByValidatedTrue()).thenReturn(80L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);
        final LocalDate twoDaysAgo = today.minusDays(2);

        // Day with most events is yesterday (1000 events)
        final List<DailyGrowthData> eventGrowthData = List.of(
            createMockGrowthData(twoDaysAgo, 200L, 200L),
            createMockGrowthData(yesterday, 1200L, 1000L),
            createMockGrowthData(today, 1250L, 50L)
        );

        when(eventRepository.findDailyEventCounts(any())).thenReturn(eventGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Best growth day for events should be yesterday
        assertThat(stats.getBestGrowthDayEvents(), is(notNullValue()));
        assertThat(stats.getBestGrowthDayEvents().getDate(), is(equalTo(yesterday)));
    }

    @Test
    @DisplayName("Should return null best growth day for events when no event data exists")
    public void shouldReturnNullBestGrowthDayForEventsWhenNoEventDataExists() {
        // Given: No event data
        when(userRepository.count()).thenReturn(0L);
        when(organizationRepository.count()).thenReturn(0L);
        when(userRepository.countByValidatedTrue()).thenReturn(0L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(eventRepository.findDailyEventCounts(any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Best growth day for events should be null
        assertThat(stats.getBestGrowthDayEvents(), is(nullValue()));
    }

    // ==================== Growth Data Events Series Tests ====================

    @Test
    @DisplayName("Should include event counts in growth data points")
    public void shouldIncludeEventCountsInGrowthDataPoints() {
        // Given: Event growth data for specific days
        when(userRepository.count()).thenReturn(100L);
        when(organizationRepository.count()).thenReturn(50L);
        when(userRepository.countByValidatedTrue()).thenReturn(80L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());

        final LocalDate today = LocalDate.now();
        final LocalDate yesterday = today.minusDays(1);

        final List<DailyGrowthData> eventGrowthData = List.of(
            createMockGrowthData(yesterday, 500L, 500L),
            createMockGrowthData(today, 750L, 250L)
        );

        when(eventRepository.findDailyEventCounts(any())).thenReturn(eventGrowthData);

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: Growth data points should include event counts
        final Optional<GrowthDataPoint> yesterdayPoint = stats.getGrowthData().stream()
            .filter(d -> d.getDate().equals(yesterday))
            .findFirst();

        assertThat(yesterdayPoint.isPresent(), is(true));
        if (yesterdayPoint.isPresent()) {
            assertThat(yesterdayPoint.get().getNewEvents(), is(equalTo(500)));
        }
    }

    @Test
    @DisplayName("Should return zero event counts for days without events")
    public void shouldReturnZeroEventCountsForDaysWithoutEvents() {
        // Given: No event data
        when(userRepository.count()).thenReturn(10L);
        when(organizationRepository.count()).thenReturn(5L);
        when(userRepository.countByValidatedTrue()).thenReturn(8L);
        when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        when(eventRepository.findDailyEventCounts(any())).thenReturn(Collections.emptyList());

        // When: Getting admin stats
        final AdminStatsResponse stats = adminStatsService.getAdminStats(30);

        // Then: All growth data points should have zero event counts
        for (final GrowthDataPoint point : stats.getGrowthData()) {
            assertThat(point.getNewEvents(), is(equalTo(0)));
        }
    }

    // ==================== Storage Stats Tests ====================

    @Test
    @DisplayName("Should return storage stats for all 5 tables")
    public void shouldReturnStorageStatsForAll5Tables() {
        // Given: No special setup needed (storage uses native queries)

        // When: Getting storage stats
        final List<TableSizeEntry> storageStats = adminStatsService.getStorageStats();

        // Then: Should return entries for all 5 tables
        assertThat(storageStats, is(notNullValue()));
        assertThat(storageStats, hasSize(5));
    }

    @Test
    @DisplayName("Should return storage entries with required fields")
    public void shouldReturnStorageEntriesWithRequiredFields() {
        // Given: No special setup needed

        // When: Getting storage stats
        final List<TableSizeEntry> storageStats = adminStatsService.getStorageStats();

        // Then: Each entry should have tableName, sizeBytes, and sizeFormatted
        for (final TableSizeEntry entry : storageStats) {
            assertThat(entry.getTableName(), is(notNullValue()));
            assertThat(entry.getTableName(), not(emptyString()));
            assertThat(entry.getSizeBytes(), is(notNullValue()));
            assertThat(entry.getSizeFormatted(), is(notNullValue()));
        }
    }

    @Test
    @DisplayName("Should return storage entries for expected table names")
    public void shouldReturnStorageEntriesForExpectedTableNames() {
        // Given: No special setup needed

        // When: Getting storage stats
        final List<TableSizeEntry> storageStats = adminStatsService.getStorageStats();

        // Then: Table names should include the 5 expected tables
        final List<String> tableNames = storageStats.stream()
            .map(TableSizeEntry::getTableName)
            .toList();

        assertThat(tableNames, hasItems("event", "notification", "channel", "organization", "app_user"));
    }
}
