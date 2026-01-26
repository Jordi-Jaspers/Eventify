package io.github.eventify.api.monitor.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelGroup;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.api.monitor.model.MonitorFilters;
import io.github.eventify.api.monitor.model.MonitorResult;
import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.monitor.model.request.MonitorRequest;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.eventify.api.watchlist.model.WatchlistFilters;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for MonitorService business logic.
 */
@DisplayName("Unit Test - Monitor Service")
@MockitoSettings(strictness = Strictness.LENIENT)
class MonitorServiceTest extends UnitTest {

    private MonitorService monitorService;
    private WatchlistRepository watchlistRepository;
    private ChannelRepository channelRepository;
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        watchlistRepository = mock(WatchlistRepository.class);
        channelRepository = mock(ChannelRepository.class);
        eventRepository = mock(EventRepository.class);

        monitorService = new MonitorService(
            watchlistRepository,
            channelRepository,
            eventRepository
        );
    }

    // ========================= BASIC MONITOR TESTS =========================

    @Test
    @DisplayName("Should return monitor response when watchlist exists")
    void shouldReturnMonitorResponseWhenWatchlistExists() {
        // Given: Watchlist with channels
        final User user = aValidUser();
        final Watchlist watchlist = aWatchlist(1L, "My Watchlist", user, null);
        final Channel channel = aChannel(1L, "test-channel", ChannelStatus.ACTIVE);
        final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);

        given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
        given(channelRepository.findAllById(anyList()))
            .willReturn(List.of(channel));
        given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
            .willReturn(new ArrayList<>());

        // When: Getting monitor data
        final MonitorResult result = monitorService.monitorWatchlist(1L, request);

        // Then: Response should contain watchlist data
        assertThat(result, is(notNullValue()));
        assertThat(result.getWatchlist().getId(), is(equalTo(1L)));
        assertThat(result.getWatchlist().getName(), is(equalTo("My Watchlist")));
        assertThat(result.getConfiguration().getChannels(), hasSize(1));
        assertThat(result.getConfiguration().getGroups(), is(empty()));
        assertThat(result.getConfiguration().getTimeline(), is(notNullValue()));
        assertThat(result.getFilters(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should throw exception when watchlist not found")
    void shouldThrowExceptionWhenWatchlistNotFound() {
        // Given: Watchlist does not exist
        final MonitorRequest request = aMonitorRequestWithTimeRange(999L, TimeRange.LAST_24H);
        given(watchlistRepository.findById(999L)).willReturn(Optional.empty());

        // When/Then: Should throw DataNotFoundException
        assertThrows(DataNotFoundException.class, () -> {
            monitorService.monitorWatchlist(999L, request);
        });
    }

    @Test
    @DisplayName("Should apply watchlist defaults when no filters provided")
    void shouldApplyWatchlistDefaultsWhenNoFiltersProvided() {
        // Given: Request with null filters, watchlist has defaults
        final User user = aValidUser();
        final Watchlist watchlist = aWatchlist(1L, "My Watchlist", user, null);
        watchlist.getFilters().setOnlyCritical(true);
        watchlist.getFilters().setSortBySeverity(false);
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);

        given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
        given(channelRepository.findAllById(anyList())).willReturn(new ArrayList<>());

        // When: Getting monitor data
        final MonitorResult result = monitorService.monitorWatchlist(1L, request);

        // Then: Should apply watchlist defaults
        assertThat(result.getFilters().getOnlyCritical(), is(true));
        assertThat(result.getFilters().getSortBySeverity(), is(false));
    }

    // ========================= FILTER TESTS =========================

    @Nested
    @DisplayName("Only Critical Filter")
    class OnlyCriticalFilterTests {

        @Test
        @DisplayName("Should filter to only critical channels in ungrouped view")
        void shouldFilterToOnlyCriticalChannelsInUngroupedView() {
            // Given: Request with onlyCritical=true and groupedView=false
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlist(1L, "My Watchlist", user, null);
            final Channel okChannel = aChannel(1L, "ok-channel", ChannelStatus.ACTIVE);
            final Channel criticalChannel = aChannel(2L, "critical-channel", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);
            request.setFilters(MonitorFilters.builder().onlyCritical(true).groupedView(false).build());

            final Event okEvent = anEvent(1L, okChannel, Severity.OK);
            final Event criticalEvent = anEvent(2L, criticalChannel, Severity.CRITICAL);

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(okChannel, criticalChannel));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(List.of(okEvent, criticalEvent));

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Should only include critical channel
            assertThat(result.getConfiguration().getChannels(), hasSize(1));
            assertThat(result.getConfiguration().getChannels().get(0).getId(), is(equalTo(2L)));
        }

        @Test
        @DisplayName("Should filter channels within groups when onlyCritical is true in grouped view")
        void shouldFilterChannelsWithinGroupsWhenOnlyCriticalInGroupedView() {
            // Given: Request with onlyCritical=true and groupedView=true (default)
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlistWithGroup(1L, "My Watchlist", user);
            final Channel okChannel = aChannel(1L, "ok-channel", ChannelStatus.ACTIVE);
            final Channel criticalChannel = aChannel(2L, "critical-channel", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);
            request.setFilters(MonitorFilters.builder().onlyCritical(true).groupedView(true).build());

            final Event okEvent = anEvent(1L, okChannel, Severity.OK);
            final Event criticalEvent = anEvent(2L, criticalChannel, Severity.CRITICAL);

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(okChannel, criticalChannel));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(List.of(okEvent, criticalEvent));

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Group should only contain critical channel
            assertThat(result.getConfiguration().getGroups(), hasSize(1));
            assertThat(result.getConfiguration().getGroups().get(0).getChannels(), hasSize(1));
            assertThat(result.getConfiguration().getGroups().get(0).getChannels().get(0).getId(), is(equalTo(2L)));
        }

        @Test
        @DisplayName("Should remove empty groups when all channels are filtered out")
        void shouldRemoveEmptyGroupsWhenAllChannelsFilteredOut() {
            // Given: Group with only non-critical channels
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlistWithGroup(1L, "My Watchlist", user);
            final Channel okChannel1 = aChannel(1L, "ok-channel-1", ChannelStatus.ACTIVE);
            final Channel okChannel2 = aChannel(2L, "ok-channel-2", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);
            request.setFilters(MonitorFilters.builder().onlyCritical(true).groupedView(true).build());

            final Event okEvent1 = anEvent(1L, okChannel1, Severity.OK);
            final Event okEvent2 = anEvent(2L, okChannel2, Severity.WARNING);

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(okChannel1, okChannel2));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(List.of(okEvent1, okEvent2));

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Empty group should be removed
            assertThat(result.getConfiguration().getGroups(), is(empty()));
        }

        @Test
        @DisplayName("Should filter standalone channels when onlyCritical is true in grouped view")
        void shouldFilterStandaloneChannelsWhenOnlyCriticalInGroupedView() {
            // Given: Mixed config with standalone channels
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlistWithMixedConfig(1L, "My Watchlist", user);
            final Channel standaloneOk = aChannel(1L, "standalone-ok", ChannelStatus.ACTIVE);
            final Channel groupedCritical = aChannel(2L, "grouped-critical", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);
            request.setFilters(MonitorFilters.builder().onlyCritical(true).groupedView(true).build());

            final Event okEvent = anEvent(1L, standaloneOk, Severity.OK);
            final Event criticalEvent = anEvent(2L, groupedCritical, Severity.CRITICAL);

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(standaloneOk, groupedCritical));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(List.of(okEvent, criticalEvent));

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Standalone OK channel should be filtered out
            assertThat(result.getConfiguration().getChannels(), is(empty()));
            // Group with critical channel should remain
            assertThat(result.getConfiguration().getGroups(), hasSize(1));
        }
    }


    @Nested
    @DisplayName("Sort By Severity Filter")
    class SortBySeverityFilterTests {

        @Test
        @DisplayName("Should sort channels by severity in ungrouped view")
        void shouldSortChannelsBySeverityInUngroupedView() {
            // Given: Request with sortBySeverity=true and groupedView=false
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlist(1L, "My Watchlist", user, null);
            final Channel okChannel = aChannel(1L, "ok-channel", ChannelStatus.ACTIVE);
            final Channel warningChannel = aChannel(2L, "warning-channel", ChannelStatus.ACTIVE);
            final Channel criticalChannel = aChannel(3L, "critical-channel", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);
            request.setFilters(MonitorFilters.builder().sortBySeverity(true).groupedView(false).build());

            final Event okEvent = anEvent(1L, okChannel, Severity.OK);
            final Event warningEvent = anEvent(2L, warningChannel, Severity.WARNING);
            final Event criticalEvent = anEvent(3L, criticalChannel, Severity.CRITICAL);

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList()))
                .willReturn(List.of(okChannel, warningChannel, criticalChannel));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(List.of(okEvent, warningEvent, criticalEvent));

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Should sort by severity (CRITICAL, WARNING, OK)
            assertThat(result.getConfiguration().getChannels(), hasSize(3));
            assertThat(result.getConfiguration().getChannels().get(0).getId(), is(equalTo(3L)));
            assertThat(result.getConfiguration().getChannels().get(1).getId(), is(equalTo(2L)));
            assertThat(result.getConfiguration().getChannels().get(2).getId(), is(equalTo(1L)));
        }

        @Test
        @DisplayName("Should sort channels within groups by severity in grouped view")
        void shouldSortChannelsWithinGroupsBySeverityInGroupedView() {
            // Given: Request with sortBySeverity=true and groupedView=true
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlistWithGroup(1L, "My Watchlist", user);
            final Channel okChannel = aChannel(1L, "ok-channel", ChannelStatus.ACTIVE);
            final Channel criticalChannel = aChannel(2L, "critical-channel", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);
            request.setFilters(MonitorFilters.builder().sortBySeverity(true).groupedView(true).build());

            final Event okEvent = anEvent(1L, okChannel, Severity.OK);
            final Event criticalEvent = anEvent(2L, criticalChannel, Severity.CRITICAL);

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(okChannel, criticalChannel));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(List.of(okEvent, criticalEvent));

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Channels within group should be sorted by severity
            assertThat(result.getConfiguration().getGroups(), hasSize(1));
            final List<Channel> groupChannels = result.getConfiguration().getGroups().get(0).getChannels();
            assertThat(groupChannels, hasSize(2));
            assertThat(groupChannels.get(0).getId(), is(equalTo(2L))); // Critical first
            assertThat(groupChannels.get(1).getId(), is(equalTo(1L))); // OK second
        }

        @Test
        @DisplayName("Should keep groups in configured order even when sortBySeverity is true")
        void shouldKeepGroupsInConfiguredOrderWhenSortBySeverity() {
            // Given: Multiple groups, first group has OK, second group has CRITICAL
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlistWithMultipleGroups(1L, "My Watchlist", user);
            final Channel okChannel = aChannel(1L, "ok-channel", ChannelStatus.ACTIVE);
            final Channel criticalChannel = aChannel(2L, "critical-channel", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);
            request.setFilters(MonitorFilters.builder().sortBySeverity(true).groupedView(true).build());

            final Event okEvent = anEvent(1L, okChannel, Severity.OK);
            final Event criticalEvent = anEvent(2L, criticalChannel, Severity.CRITICAL);

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(okChannel, criticalChannel));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(List.of(okEvent, criticalEvent));

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Groups should remain in configured order (Group A first, Group B second)
            assertThat(result.getConfiguration().getGroups(), hasSize(2));
            assertThat(result.getConfiguration().getGroups().get(0).getName(), is(equalTo("Group A")));
            assertThat(result.getConfiguration().getGroups().get(1).getName(), is(equalTo("Group B")));
        }

        @Test
        @DisplayName("Should sort standalone channels by severity in grouped view")
        void shouldSortStandaloneChannelsBySeverityInGroupedView() {
            // Given: Mixed config with standalone channels
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlistWithStandaloneChannels(1L, "My Watchlist", user);
            final Channel okChannel = aChannel(1L, "ok-channel", ChannelStatus.ACTIVE);
            final Channel criticalChannel = aChannel(2L, "critical-channel", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);
            request.setFilters(MonitorFilters.builder().sortBySeverity(true).groupedView(true).build());

            final Event okEvent = anEvent(1L, okChannel, Severity.OK);
            final Event criticalEvent = anEvent(2L, criticalChannel, Severity.CRITICAL);

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(okChannel, criticalChannel));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(List.of(okEvent, criticalEvent));

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Standalone channels should be sorted by severity
            assertThat(result.getConfiguration().getChannels(), hasSize(2));
            assertThat(result.getConfiguration().getChannels().get(0).getId(), is(equalTo(2L))); // Critical first
            assertThat(result.getConfiguration().getChannels().get(1).getId(), is(equalTo(1L))); // OK second
        }
    }


    @Nested
    @DisplayName("Grouped View Filter")
    class GroupedViewFilterTests {

        @Test
        @DisplayName("Should flatten groups into channels list when groupedView is false")
        void shouldFlattenGroupsWhenGroupedViewIsFalse() {
            // Given: Watchlist with groups, request with groupedView=false
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlistWithGroup(1L, "My Watchlist", user);
            final Channel channel1 = aChannel(1L, "channel-1", ChannelStatus.ACTIVE);
            final Channel channel2 = aChannel(2L, "channel-2", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);
            request.setFilters(MonitorFilters.builder().groupedView(false).build());

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(channel1, channel2));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(new ArrayList<>());

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Groups should be empty, all channels in flat list
            assertThat(result.getConfiguration().getGroups(), is(empty()));
            assertThat(result.getConfiguration().getChannels(), hasSize(2));
        }

        @Test
        @DisplayName("Should keep groups when groupedView is true")
        void shouldKeepGroupsWhenGroupedViewIsTrue() {
            // Given: Watchlist with groups, request with groupedView=true
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlistWithGroup(1L, "My Watchlist", user);
            final Channel channel1 = aChannel(1L, "channel-1", ChannelStatus.ACTIVE);
            final Channel channel2 = aChannel(2L, "channel-2", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);
            request.setFilters(MonitorFilters.builder().groupedView(true).build());

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(channel1, channel2));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(new ArrayList<>());

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Groups should remain
            assertThat(result.getConfiguration().getGroups(), hasSize(1));
            assertThat(result.getConfiguration().getGroups().get(0).getName(), is(equalTo("API Services")));
        }
    }

    @Test
    @DisplayName("Should use custom date range when provided")
    void shouldUseCustomDateRangeWhenProvided() {
        // Given: Request with custom date range
        final User user = aValidUser();
        final Watchlist watchlist = aWatchlist(1L, "My Watchlist", user, null);
        final OffsetDateTime customStart = OffsetDateTime.now().minusDays(5);
        final OffsetDateTime customEnd = OffsetDateTime.now().minusDays(2);
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            MonitorFilters.builder()
                .startTime(customStart)
                .endTime(customEnd)
                .build()
        );

        given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
        given(channelRepository.findAllById(anyList())).willReturn(new ArrayList<>());

        // When: Getting monitor data
        final MonitorResult result = monitorService.monitorWatchlist(1L, request);

        // Then: Response should use custom range
        assertThat(result.getTimeRange().getStart(), is(equalTo(customStart)));
        assertThat(result.getTimeRange().getEnd(), is(equalTo(customEnd)));
        assertThat(result.getTimeRange().isLive(), is(false));
    }

    @Test
    @DisplayName("Should return live mode when using preset time range")
    void shouldReturnLiveModeWhenUsingPresetTimeRange() {
        // Given: Request with preset time range
        final User user = aValidUser();
        final Watchlist watchlist = aWatchlist(1L, "My Watchlist", user, null);
        final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);

        given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
        given(channelRepository.findAllById(anyList())).willReturn(new ArrayList<>());

        // When: Getting monitor data
        final MonitorResult result = monitorService.monitorWatchlist(1L, request);

        // Then: Should be in live mode
        assertThat(result.getTimeRange().isLive(), is(true));
    }

    // ========================= CHANNEL GROUP TESTS =========================

    @Nested
    @DisplayName("Channel Groups")
    class ChannelGroupTests {

        @Test
        @DisplayName("Should return enriched groups with consolidated timelines")
        void shouldReturnEnrichedGroupsWithConsolidatedTimelines() {
            // Given: Watchlist with a channel group containing two channels
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlistWithGroup(1L, "My Watchlist", user);
            final Channel channel1 = aChannel(1L, "api-service", ChannelStatus.ACTIVE);
            final Channel channel2 = aChannel(2L, "auth-service", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);

            // Events for the group channels
            final Event okEvent = anEvent(1L, channel1, Severity.OK);
            final Event warningEvent = anEvent(2L, channel2, Severity.WARNING);

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(channel1, channel2));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(List.of(okEvent, warningEvent));

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Should have one group with two channels
            assertThat(result.getConfiguration().getChannels(), is(empty())); // No standalone channels
            assertThat(result.getConfiguration().getGroups(), hasSize(1));

            final ChannelGroup group = result.getConfiguration().getGroups().get(0);
            assertThat(group.getName(), is(equalTo("API Services")));
            assertThat(group.getChannels(), hasSize(2));

            // Group timeline should be consolidated (worst severity = WARNING)
            assertThat(group.getTimeline(), is(notNullValue()));
            assertThat(group.getTimeline().getDurations(), is(not(empty())));
        }

        @Test
        @DisplayName("Should consolidate dashboard from both standalone channels and groups")
        void shouldConsolidateDashboardFromBothChannelsAndGroups() {
            // Given: Watchlist with standalone channel and a group
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlistWithMixedConfig(1L, "My Watchlist", user);
            final Channel standaloneChannel = aChannel(1L, "standalone", ChannelStatus.ACTIVE);
            final Channel groupedChannel = aChannel(2L, "grouped", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);

            final Event criticalEvent = anEvent(1L, standaloneChannel, Severity.CRITICAL);
            final Event okEvent = anEvent(2L, groupedChannel, Severity.OK);

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(standaloneChannel, groupedChannel));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(List.of(criticalEvent, okEvent));

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Should have both standalone channel and group
            assertThat(result.getConfiguration().getChannels(), hasSize(1));
            assertThat(result.getConfiguration().getGroups(), hasSize(1));

            // Dashboard should be consolidated from both (worst = CRITICAL)
            assertThat(result.getConfiguration().getTimeline(), is(notNullValue()));
            assertThat(result.getConfiguration().getTimeline().getDurations(), is(not(empty())));
        }

        @Test
        @DisplayName("Should return empty groups list when no groups configured")
        void shouldReturnEmptyGroupsListWhenNoGroupsConfigured() {
            // Given: Watchlist with only standalone channels (no groups)
            final User user = aValidUser();
            final Watchlist watchlist = aWatchlist(1L, "My Watchlist", user, null);
            final Channel channel = aChannel(1L, "test-channel", ChannelStatus.ACTIVE);
            final MonitorRequest request = aMonitorRequestWithTimeRange(1L, TimeRange.LAST_24H);

            given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
            given(channelRepository.findAllById(anyList())).willReturn(List.of(channel));
            given(eventRepository.findEventsWithLastBeforeRange(anyList(), any(), any()))
                .willReturn(new ArrayList<>());

            // When: Getting monitor data
            final MonitorResult result = monitorService.monitorWatchlist(1L, request);

            // Then: Groups should be empty
            assertThat(result.getConfiguration().getChannels(), hasSize(1));
            assertThat(result.getConfiguration().getGroups(), is(empty()));
        }
    }

    // ========================= HELPER METHODS =========================

    private Watchlist aWatchlist(final Long id, final String name, final User user, final Organization org) {
        final Watchlist watchlist = new Watchlist(name, user, org);
        watchlist.setId(id);
        watchlist.setConfiguration(
            WatchlistConfiguration.builder()
                .channels(channelsWithIds(1L, 2L, 3L))
                .groups(new ArrayList<>())
                .build()
        );
        watchlist.setFilters(WatchlistFilters.defaults());
        return watchlist;
    }

    private Watchlist aWatchlistWithGroup(final Long id, final String name, final User user) {
        final Watchlist watchlist = new Watchlist(name, user, null);
        watchlist.setId(id);
        watchlist.setConfiguration(
            WatchlistConfiguration.builder()
                .channels(new ArrayList<>()) // No standalone channels
                .groups(
                    List.of(
                        ChannelGroup.builder()
                            .id(UUID.randomUUID())
                            .name("API Services")
                            .channels(channelsWithIds(1L, 2L))
                            .build()
                    )
                )
                .build()
        );
        watchlist.setFilters(WatchlistFilters.defaults());
        return watchlist;
    }

    private Watchlist aWatchlistWithMixedConfig(final Long id, final String name, final User user) {
        final Watchlist watchlist = new Watchlist(name, user, null);
        watchlist.setId(id);
        watchlist.setConfiguration(
            WatchlistConfiguration.builder()
                .channels(channelsWithIds(1L)) // One standalone channel
                .groups(
                    List.of(
                        ChannelGroup.builder()
                            .id(UUID.randomUUID())
                            .name("Database Services")
                            .channels(channelsWithIds(2L))
                            .build()
                    )
                )
                .build()
        );
        watchlist.setFilters(WatchlistFilters.defaults());
        return watchlist;
    }

    private Watchlist aWatchlistWithMultipleGroups(final Long id, final String name, final User user) {
        final Watchlist watchlist = new Watchlist(name, user, null);
        watchlist.setId(id);
        watchlist.setConfiguration(
            WatchlistConfiguration.builder()
                .channels(new ArrayList<>())
                .groups(
                    List.of(
                        ChannelGroup.builder()
                            .id(UUID.randomUUID())
                            .name("Group A")
                            .channels(channelsWithIds(1L)) // OK channel
                            .build(),
                        ChannelGroup.builder()
                            .id(UUID.randomUUID())
                            .name("Group B")
                            .channels(channelsWithIds(2L)) // CRITICAL channel
                            .build()
                    )
                )
                .build()
        );
        watchlist.setFilters(WatchlistFilters.defaults());
        return watchlist;
    }

    private Watchlist aWatchlistWithStandaloneChannels(final Long id, final String name, final User user) {
        final Watchlist watchlist = new Watchlist(name, user, null);
        watchlist.setId(id);
        watchlist.setConfiguration(
            WatchlistConfiguration.builder()
                .channels(channelsWithIds(1L, 2L)) // Two standalone channels
                .groups(new ArrayList<>())
                .build()
        );
        watchlist.setFilters(WatchlistFilters.defaults());
        return watchlist;
    }

    private Channel aChannel(final Long id, final String name, final ChannelStatus status) {
        final Channel channel = new Channel();
        channel.setId(id);
        channel.setName(name);
        channel.setStatus(status);
        return channel;
    }

    private List<Channel> channelsWithIds(final Long... ids) {
        return java.util.Arrays.stream(ids)
            .map(channelId -> {
                final Channel channel = new Channel();
                channel.setId(channelId);
                return channel;
            })
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private MonitorRequest aMonitorRequestWithTimeRange(final Long watchlistId, final TimeRange timeRange) {
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlistId);
        request.setFilters(MonitorFilters.builder().timeRange(timeRange).build());
        return request;
    }

    private Event anEvent(final Long id, final Channel channel, final Severity severity) {
        final Event event = new Event();
        event.setId(id);
        event.setChannel(channel);
        event.setSeverity(severity);
        event.setTitle("Test Event");
        event.setTimestamp(OffsetDateTime.now().minusMinutes(30));
        return event;
    }
}
