package io.github.eventify.api.admin.stats.service;

import io.github.eventify.api.admin.stats.model.AdminEventVolume;
import io.github.eventify.api.admin.stats.model.AdminGrowth;
import io.github.eventify.api.admin.stats.model.mapper.AdminStatsMapper;
import io.github.eventify.api.admin.stats.repository.AdminStorageRepository;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.support.UnitTest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@DisplayName("Unit Test - Admin Stats Service (Date Range)")
public class AdminStatsServiceDateRangeTest extends UnitTest {

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

    @Mock
    private AdminStatsMapper adminStatsMapper;

    @BeforeEach
    public void setUp() {
        adminStatsService = new AdminStatsService(
            userRepository,
            organizationRepository,
            channelRepository,
            eventRepository,
            adminStorageRepository,
            adminStatsMapper
        );

        lenient().when(eventRepository.countByTimestampAfter(any())).thenReturn(0L);
        lenient().when(eventRepository.findDailyEventCounts(any())).thenReturn(Collections.emptyList());
        lenient().when(userRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        lenient().when(organizationRepository.findDailyGrowthCounts(any(), any())).thenReturn(Collections.emptyList());
        lenient().when(userRepository.count()).thenReturn(0L);
        lenient().when(organizationRepository.count()).thenReturn(0L);
        lenient().when(userRepository.countByValidatedTrue()).thenReturn(0L);
    }

    // ==================== getAdminGrowth with explicit date range ====================

    @Test
    @DisplayName("Should use explicit startDate and endDate when provided to getAdminGrowth")
    public void shouldUseExplicitDatesInGetAdminGrowth() {
        // Given: explicit date range of 10 days
        final LocalDate startDate = LocalDate.now().minusDays(10);
        final LocalDate endDate = LocalDate.now();

        // When: calling getAdminGrowth with explicit dates
        final AdminGrowth growth = adminStatsService.getAdminGrowth(startDate, endDate);

        // Then: result contains exactly the expected number of data points (10 days + today = 11)
        assertThat(growth, is(notNullValue()));
        assertThat(growth.getGrowthData(), hasSize(11));
    }

    @Test
    @DisplayName("Should produce 1 data point when startDate equals endDate in getAdminGrowth")
    public void shouldProduce1DataPointWhenStartEqualsEndInGrowth() {
        // Given: single-day range
        final LocalDate singleDay = LocalDate.now().minusDays(1);

        // When: calling getAdminGrowth with same start and end
        final AdminGrowth growth = adminStatsService.getAdminGrowth(singleDay, singleDay);

        // Then: exactly 1 data point
        assertThat(growth.getGrowthData(), hasSize(1));
    }

    @Test
    @DisplayName("Should query repositories with correct timestamps when explicit dates are used for getAdminGrowth")
    public void shouldQueryRepositoriesWithCorrectTimestampsForGrowth() {
        // Given: explicit date range
        final LocalDate startDate = LocalDate.now().minusDays(5);
        final LocalDate endDate = LocalDate.now();

        final ArgumentCaptor<OffsetDateTime> startCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        final ArgumentCaptor<OffsetDateTime> endCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        // When: calling getAdminGrowth with explicit dates
        adminStatsService.getAdminGrowth(startDate, endDate);

        // Then: repositories are queried with timestamps derived from startDate
        verify(userRepository).findDailyGrowthCounts(startCaptor.capture(), endCaptor.capture());

        final OffsetDateTime capturedStart = startCaptor.getValue();
        assertThat(capturedStart.toLocalDate(), is(equalTo(startDate)));
    }

    @Test
    @DisplayName("Should still work with days param for getAdminGrowth (backward compatible)")
    public void shouldWorkWithDaysParamForGrowthBackwardCompatible() {
        // Given: days=7
        // When: calling getAdminGrowth with days
        final AdminGrowth growth = adminStatsService.getAdminGrowth(7);

        // Then: result contains 8 data points (7 days + today)
        assertThat(growth, is(notNullValue()));
        assertThat(growth.getGrowthData(), hasSize(8));
    }

    @Test
    @DisplayName("Should return different data ranges for days=30 vs explicit 30-day window ending earlier")
    public void shouldReturnDifferentRangesForDaysVsExplicitDates() {
        // Given: days=30 produces today-relative range; explicit dates produce fixed range
        final LocalDate explicitStart = LocalDate.now().minusDays(60);
        final LocalDate explicitEnd = LocalDate.now().minusDays(30);

        // When: calling both variants
        final AdminGrowth byDays = adminStatsService.getAdminGrowth(30);
        final AdminGrowth byDates = adminStatsService.getAdminGrowth(explicitStart, explicitEnd);

        // Then: both have 31 data points (same size but different date ranges)
        assertThat(byDays.getGrowthData(), hasSize(31));
        assertThat(byDates.getGrowthData(), hasSize(31));

        // And: first data point dates differ
        final LocalDate byDaysFirstDate = byDays.getGrowthData().get(0).getDate();
        final LocalDate byDatesFirstDate = byDates.getGrowthData().get(0).getDate();
        assertThat(byDaysFirstDate, is(not(equalTo(byDatesFirstDate))));
    }

    // ==================== getEventVolume with explicit date range ====================

    @Test
    @DisplayName("Should use explicit startDate and endDate when provided to getEventVolume")
    public void shouldUseExplicitDatesInGetEventVolume() {
        // Given: explicit date range of 14 days
        final LocalDate startDate = LocalDate.now().minusDays(14);
        final LocalDate endDate = LocalDate.now();

        // When: calling getEventVolume with explicit dates
        final AdminEventVolume volume = adminStatsService.getEventVolume(startDate, endDate);

        // Then: daily volume contains exactly 15 data points (14 days + today)
        assertThat(volume, is(notNullValue()));
        assertThat(volume.getDailyVolume(), hasSize(15));
    }

    @Test
    @DisplayName("Should produce 1 volume data point when startDate equals endDate in getEventVolume")
    public void shouldProduce1VolumeDataPointWhenStartEqualsEnd() {
        // Given: single-day range
        final LocalDate singleDay = LocalDate.now().minusDays(3);

        // When: calling getEventVolume with same start and end
        final AdminEventVolume volume = adminStatsService.getEventVolume(singleDay, singleDay);

        // Then: exactly 1 daily data point
        assertThat(volume.getDailyVolume(), hasSize(1));
    }

    @Test
    @DisplayName("Should query event repository with timestamp derived from explicit startDate for getEventVolume")
    public void shouldQueryEventRepositoryWithCorrectTimestampForEventVolume() {
        // Given: explicit start/end dates
        final LocalDate startDate = LocalDate.now().minusDays(7);
        final LocalDate endDate = LocalDate.now();

        final ArgumentCaptor<OffsetDateTime> startCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        // When: calling getEventVolume with explicit dates
        adminStatsService.getEventVolume(startDate, endDate);

        // Then: event repository queried with correct start timestamp
        verify(eventRepository).findDailyEventCounts(startCaptor.capture());
        assertThat(startCaptor.getValue().toLocalDate(), is(equalTo(startDate)));
    }

    @Test
    @DisplayName("Should still work with days param for getEventVolume (backward compatible)")
    public void shouldWorkWithDaysParamForEventVolumeBackwardCompatible() {
        // Given: days=7
        // When: calling getEventVolume with days
        final AdminEventVolume volume = adminStatsService.getEventVolume(7);

        // Then: daily volume contains 8 entries (7 days + today)
        assertThat(volume, is(notNullValue()));
        assertThat(volume.getDailyVolume(), hasSize(8));
    }

    @Test
    @DisplayName("Should default data points to zero eventCount for dates with no events")
    public void shouldDefaultDataPointsToZeroWhenNoEvents() {
        // Given: no events in repository (already lenient-mocked to empty)
        final LocalDate startDate = LocalDate.now().minusDays(3);
        final LocalDate endDate = LocalDate.now();

        // When: calling getEventVolume
        final AdminEventVolume volume = adminStatsService.getEventVolume(startDate, endDate);

        // Then: all data points have zero eventCount
        volume.getDailyVolume().forEach(
            point -> assertThat(point.getEventCount(), is(equalTo(0L)))
        );
    }

    // ==================== Cache key differentiation ====================

    @Test
    @DisplayName("Should distinguish date range from days param by returning date-anchored data points")
    public void shouldDistinguishDateRangeFromDaysParam() {
        // Given: days=30 is computed from today; explicit dates are fixed in the past
        final LocalDate explicitStart = LocalDate.now().minusDays(90);
        final LocalDate explicitEnd = LocalDate.now().minusDays(60);

        // When: calling getAdminGrowth both ways
        final AdminGrowth byDays = adminStatsService.getAdminGrowth(30);
        final AdminGrowth byDates = adminStatsService.getAdminGrowth(explicitStart, explicitEnd);

        // Then: first data-point dates are different, confirming independent cache keys
        final LocalDate byDaysStart = byDays.getGrowthData().get(0).getDate();
        final LocalDate byDatesStart = byDates.getGrowthData().get(0).getDate();
        assertThat(byDaysStart, is(not(equalTo(byDatesStart))));
        assertThat(byDatesStart, is(equalTo(explicitStart)));
    }
}
