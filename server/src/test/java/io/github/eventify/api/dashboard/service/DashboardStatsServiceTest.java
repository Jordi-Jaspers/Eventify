package io.github.eventify.api.dashboard.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.dashboard.model.response.DashboardStatsResponse;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("Unit Test - Dashboard Stats Service")
public class DashboardStatsServiceTest extends UnitTest {

    private DashboardStatsService dashboardStatsService;
    private ChannelRepository channelRepository;
    private EventRepository eventRepository;

    @BeforeEach
    public void setUp() {
        channelRepository = mock(ChannelRepository.class);
        eventRepository = mock(EventRepository.class);
        dashboardStatsService = new DashboardStatsService(channelRepository, eventRepository);
    }

    @Test
    @DisplayName("Should calculate error rate correctly when channels have critical events")
    public void shouldCalculateErrorRateCorrectlyWhenChannelsHaveCriticalEvents() {
        // Given: 10 active channels, 2 with last event severity=CRITICAL
        final User user = aValidUser();
        final List<Channel> channels = createMockChannels(10, user, null);
        given(channelRepository.findByUserIdAndOrganizationIsNullAndStatus(user.getId(), ChannelStatus.ACTIVE))
            .willReturn(channels);

        // And: 2 channels have CRITICAL as last event
        final Event criticalEvent1 = createMockEvent(channels.get(0), Severity.CRITICAL, OffsetDateTime.now());
        final Event criticalEvent2 = createMockEvent(channels.get(1), Severity.CRITICAL, OffsetDateTime.now());
        final Event okEvent = createMockEvent(channels.get(2), Severity.OK, OffsetDateTime.now());

        given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channels.get(0).getId()))
            .willReturn(Optional.of(criticalEvent1));
        given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channels.get(1).getId()))
            .willReturn(Optional.of(criticalEvent2));
        for (int i = 2; i < 10; i++) {
            given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channels.get(i).getId()))
                .willReturn(Optional.of(okEvent));
        }

        // When: Getting personal dashboard stats
        final DashboardStatsResponse stats = dashboardStatsService.getPersonalStats(user.getId());

        // Then: Error rate should be 20.0%
        assertThat(stats.errorRate(), is(equalTo(20.0)));
    }

    @Test
    @DisplayName("Should return zero stats when user has no channels")
    public void shouldReturnZeroStatsWhenUserHasNoChannels() {
        // Given: User with no channels
        final User user = aValidUser();
        given(channelRepository.findByUserIdAndOrganizationIsNullAndStatus(user.getId(), ChannelStatus.ACTIVE))
            .willReturn(List.of());
        given(eventRepository.countByChannelIdInAndTimestampAfter(any(), any()))
            .willReturn(0L);

        // When: Getting personal dashboard stats
        final DashboardStatsResponse stats = dashboardStatsService.getPersonalStats(user.getId());

        // Then: All stats should be zero
        assertThat(stats.eventsToday(), is(equalTo(0L)));
        assertThat(stats.activeChannels(), is(equalTo(0)));
        assertThat(stats.errorRate(), is(equalTo(0.0)));
        assertThat(stats.lastEventAt(), is(nullValue()));
    }

    @Test
    @DisplayName("Should count events from last 24 hours only")
    public void shouldCountEventsFromLast24HoursOnly() {
        // Given: User with active channels
        final User user = aValidUser();
        final List<Channel> channels = createMockChannels(3, user, null);
        given(channelRepository.findByUserIdAndOrganizationIsNullAndStatus(user.getId(), ChannelStatus.ACTIVE))
            .willReturn(channels);

        // And: Events created today
        final long expectedEventCount = 15L;
        given(eventRepository.countByChannelIdInAndTimestampAfter(any(), any()))
            .willReturn(expectedEventCount);

        // And: Mock last events for channels
        for (final Channel channel : channels) {
            final Event okEvent = createMockEvent(channel, Severity.OK, OffsetDateTime.now());
            given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channel.getId()))
                .willReturn(Optional.of(okEvent));
        }

        // When: Getting personal dashboard stats
        final DashboardStatsResponse stats = dashboardStatsService.getPersonalStats(user.getId());

        // Then: Events today should match expected count
        assertThat(stats.eventsToday(), is(equalTo(expectedEventCount)));
    }

    @Test
    @DisplayName("Should return correct active channels count")
    public void shouldReturnCorrectActiveChannelsCount() {
        // Given: User with 5 active channels
        final User user = aValidUser();
        final List<Channel> channels = createMockChannels(5, user, null);
        given(channelRepository.findByUserIdAndOrganizationIsNullAndStatus(user.getId(), ChannelStatus.ACTIVE))
            .willReturn(channels);

        // And: Mock events
        given(eventRepository.countByChannelIdInAndTimestampAfter(any(), any())).willReturn(0L);
        for (final Channel channel : channels) {
            given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channel.getId()))
                .willReturn(Optional.empty());
        }

        // When: Getting personal dashboard stats
        final DashboardStatsResponse stats = dashboardStatsService.getPersonalStats(user.getId());

        // Then: Active channels should be 5
        assertThat(stats.activeChannels(), is(equalTo(5)));
    }

    @Test
    @DisplayName("Should return most recent event timestamp")
    public void shouldReturnMostRecentEventTimestamp() {
        // Given: User with active channels
        final User user = aValidUser();
        final List<Channel> channels = createMockChannels(3, user, null);
        given(channelRepository.findByUserIdAndOrganizationIsNullAndStatus(user.getId(), ChannelStatus.ACTIVE))
            .willReturn(channels);

        // And: Events with different timestamps
        final OffsetDateTime now = OffsetDateTime.now();
        final OffsetDateTime oneHourAgo = now.minusHours(1);
        final OffsetDateTime twoHoursAgo = now.minusHours(2);

        given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channels.get(0).getId()))
            .willReturn(Optional.of(createMockEvent(channels.get(0), Severity.OK, twoHoursAgo)));
        given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channels.get(1).getId()))
            .willReturn(Optional.of(createMockEvent(channels.get(1), Severity.OK, now)));
        given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channels.get(2).getId()))
            .willReturn(Optional.of(createMockEvent(channels.get(2), Severity.OK, oneHourAgo)));

        given(eventRepository.countByChannelIdInAndTimestampAfter(any(), any())).willReturn(0L);

        // When: Getting personal dashboard stats
        final DashboardStatsResponse stats = dashboardStatsService.getPersonalStats(user.getId());

        // Then: Last event timestamp should be the most recent
        assertThat(stats.lastEventAt(), is(equalTo(now)));
    }

    @Test
    @DisplayName("Should filter personal channels only when getting personal stats")
    public void shouldFilterPersonalChannelsOnlyWhenGettingPersonalStats() {
        // Given: User with personal and org channels
        final User user = aValidUser();
        final List<Channel> personalChannels = createMockChannels(3, user, null);
        given(channelRepository.findByUserIdAndOrganizationIsNullAndStatus(user.getId(), ChannelStatus.ACTIVE))
            .willReturn(personalChannels);

        // And: Mock events
        given(eventRepository.countByChannelIdInAndTimestampAfter(any(), any())).willReturn(0L);
        for (final Channel channel : personalChannels) {
            given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channel.getId()))
                .willReturn(Optional.empty());
        }

        // When: Getting personal dashboard stats
        final DashboardStatsResponse stats = dashboardStatsService.getPersonalStats(user.getId());

        // Then: Should only count personal channels
        assertThat(stats.activeChannels(), is(equalTo(3)));
    }

    @Test
    @DisplayName("Should filter org channels only when getting org stats")
    public void shouldFilterOrgChannelsOnlyWhenGettingOrgStats() {
        // Given: Organization with channels
        final Long orgId = 1L;
        final List<Channel> orgChannels = createMockChannels(5, aValidUser(), new Organization());
        given(channelRepository.findByOrganizationIdAndStatus(orgId, ChannelStatus.ACTIVE))
            .willReturn(orgChannels);

        // And: Mock events
        given(eventRepository.countByChannelIdInAndTimestampAfter(any(), any())).willReturn(0L);
        for (final Channel channel : orgChannels) {
            given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channel.getId()))
                .willReturn(Optional.empty());
        }

        // When: Getting org dashboard stats
        final DashboardStatsResponse stats = dashboardStatsService.getOrganizationStats(orgId);

        // Then: Should only count org channels
        assertThat(stats.activeChannels(), is(equalTo(5)));
    }

    @Test
    @DisplayName("Should calculate error rate as zero when no channels have events")
    public void shouldCalculateErrorRateAsZeroWhenNoChannelsHaveEvents() {
        // Given: User with active channels but no events
        final User user = aValidUser();
        final List<Channel> channels = createMockChannels(5, user, null);
        given(channelRepository.findByUserIdAndOrganizationIsNullAndStatus(user.getId(), ChannelStatus.ACTIVE))
            .willReturn(channels);

        // And: No events for any channel
        for (final Channel channel : channels) {
            given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channel.getId()))
                .willReturn(Optional.empty());
        }

        given(eventRepository.countByChannelIdInAndTimestampAfter(any(), any())).willReturn(0L);

        // When: Getting personal dashboard stats
        final DashboardStatsResponse stats = dashboardStatsService.getPersonalStats(user.getId());

        // Then: Error rate should be 0.0
        assertThat(stats.errorRate(), is(equalTo(0.0)));
    }

    @Test
    @DisplayName("Should calculate error rate as 100 when all channels have critical events")
    public void shouldCalculateErrorRateAs100WhenAllChannelsHaveCriticalEvents() {
        // Given: User with 5 active channels, all with CRITICAL events
        final User user = aValidUser();
        final List<Channel> channels = createMockChannels(5, user, null);
        given(channelRepository.findByUserIdAndOrganizationIsNullAndStatus(user.getId(), ChannelStatus.ACTIVE))
            .willReturn(channels);

        // And: All channels have CRITICAL as last event
        for (final Channel channel : channels) {
            final Event criticalEvent = createMockEvent(channel, Severity.CRITICAL, OffsetDateTime.now());
            given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channel.getId()))
                .willReturn(Optional.of(criticalEvent));
        }

        given(eventRepository.countByChannelIdInAndTimestampAfter(any(), any())).willReturn(0L);

        // When: Getting personal dashboard stats
        final DashboardStatsResponse stats = dashboardStatsService.getPersonalStats(user.getId());

        // Then: Error rate should be 100.0
        assertThat(stats.errorRate(), is(equalTo(100.0)));
    }

    @Test
    @DisplayName("Should only count CRITICAL severity as error")
    public void shouldOnlyCountCriticalSeverityAsError() {
        // Given: User with channels having different severities
        final User user = aValidUser();
        final List<Channel> channels = createMockChannels(4, user, null);
        given(channelRepository.findByUserIdAndOrganizationIsNullAndStatus(user.getId(), ChannelStatus.ACTIVE))
            .willReturn(channels);

        // And: Various severity levels (only 1 CRITICAL)
        given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channels.get(0).getId()))
            .willReturn(Optional.of(createMockEvent(channels.get(0), Severity.CRITICAL, OffsetDateTime.now())));
        given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channels.get(1).getId()))
            .willReturn(Optional.of(createMockEvent(channels.get(1), Severity.WARNING, OffsetDateTime.now())));
        given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channels.get(2).getId()))
            .willReturn(Optional.of(createMockEvent(channels.get(2), Severity.OK, OffsetDateTime.now())));
        given(eventRepository.findTopByChannelIdOrderByTimestampDesc(channels.get(3).getId()))
            .willReturn(Optional.of(createMockEvent(channels.get(3), Severity.NO_DATA, OffsetDateTime.now())));

        given(eventRepository.countByChannelIdInAndTimestampAfter(any(), any())).willReturn(0L);

        // When: Getting personal dashboard stats
        final DashboardStatsResponse stats = dashboardStatsService.getPersonalStats(user.getId());

        // Then: Error rate should be 25% (1/4)
        assertThat(stats.errorRate(), is(equalTo(25.0)));
    }

    // ========================= HELPER METHODS =========================

    private List<Channel> createMockChannels(final int count, final User user, final Organization org) {
        final List<Channel> channels = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Channel channel = new Channel();
            channel.setId((long) (i + 1));
            channel.setName("Channel " + (i + 1));
            channel.setUser(user);
            channel.setOrganization(org);
            channel.setStatus(ChannelStatus.ACTIVE);
            channels.add(channel);
        }
        return channels;
    }

    private Event createMockEvent(final Channel channel, final Severity severity, final OffsetDateTime timestamp) {
        final Event event = new Event();
        event.setId(1L);
        event.setChannel(channel);
        event.setSeverity(severity);
        event.setTitle("Test Event");
        event.setTimestamp(timestamp);
        return event;
    }
}
