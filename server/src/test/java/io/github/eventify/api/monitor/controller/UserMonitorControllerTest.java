package io.github.eventify.api.monitor.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.monitor.model.request.MonitorRequest;
import io.github.eventify.api.monitor.model.response.MonitorResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.support.IntegrationTest;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.USER_MONITOR_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for UserMonitorController.
 */
@DisplayName("Integration Test - User Monitor Controller")
class UserMonitorControllerTest extends IntegrationTest {

    // ========================= SUCCESSFUL REQUESTS =========================

    @Test
    @DisplayName("Should return monitor data when valid request")
    void shouldReturnMonitorDataWhenValidRequest() throws Exception {
        // Given: User with watchlist containing channels with events
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");
        final Channel channel = aChannelForUser(user, "test-channel");
        addChannelToWatchlist(watchlist, channel);
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(1));

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Posting monitor request
        final MockHttpServletRequestBuilder requestBuilder = post(USER_MONITOR_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return monitor response
        result.andExpect(status().isOk());

        final MonitorResponse response = fromJson(
            result.andReturn().getResponse().getContentAsString(),
            MonitorResponse.class
        );

        assertThat(response, is(notNullValue()));
        assertThat(response.getWatchlistId(), is(equalTo(watchlist.getId())));
        assertThat(response.getWatchlistName(), is(equalTo("My Watchlist")));
        assertThat(response.getDashboard().getChannels(), hasSize(1));
        assertThat(response.getDashboard().getTimeline(), is(notNullValue()));
        assertThat(response.getFilters(), is(notNullValue()));
        assertThat(response.isLive(), is(true));
    }

    @Test
    @DisplayName("Should use custom date range when provided")
    void shouldUseCustomDateRangeWhenProvided() throws Exception {
        // Given: User with watchlist and custom date range
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");
        final Channel channel = aChannelForUser(user, "test-channel");
        addChannelToWatchlist(watchlist, channel);

        final OffsetDateTime customStart = OffsetDateTime.now().minusDays(5);
        final OffsetDateTime customEnd = OffsetDateTime.now().minusDays(2);

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .startTime(customStart)
                .endTime(customEnd)
                .build()
        );

        // When: Posting monitor request
        final MockHttpServletRequestBuilder requestBuilder = post(USER_MONITOR_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should use custom range and not be live
        result.andExpect(status().isOk());

        final MonitorResponse response = fromJson(
            result.andReturn().getResponse().getContentAsString(),
            MonitorResponse.class
        );

        assertThat(response.isLive(), is(false));
        assertThat(response.getRangeStart(), is(notNullValue()));
        assertThat(response.getRangeEnd(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should filter to only critical channels when requested (ungrouped view)")
    void shouldFilterToOnlyCriticalChannelsWhenRequested() throws Exception {
        // Given: User with watchlist containing OK and CRITICAL channels
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");
        final Channel okChannel = aChannelForUser(user, "ok-channel");
        final Channel criticalChannel = aChannelForUser(user, "critical-channel");
        addChannelToWatchlist(watchlist, okChannel);
        addChannelToWatchlist(watchlist, criticalChannel);

        anEventForChannel(okChannel, Severity.OK, OffsetDateTime.now().minusHours(1));
        anEventForChannel(criticalChannel, Severity.CRITICAL, OffsetDateTime.now().minusHours(1));

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .onlyCritical(true)
                .groupedView(false) // onlyCritical only applies in ungrouped view
                .build()
        );

        // When: Posting monitor request
        final MockHttpServletRequestBuilder requestBuilder = post(USER_MONITOR_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should only return critical channel
        result.andExpect(status().isOk());

        final MonitorResponse response = fromJson(
            result.andReturn().getResponse().getContentAsString(),
            MonitorResponse.class
        );

        assertThat(response.getDashboard().getChannels(), hasSize(1));
        assertThat(response.getDashboard().getChannels().get(0).getChannelName(), is(equalTo("critical-channel")));
        assertThat(response.getFilters().getOnlyCritical(), is(true));
    }

    @Test
    @DisplayName("Should return empty channels when watchlist has no channels")
    void shouldReturnEmptyChannelsWhenWatchlistHasNoChannels() throws Exception {
        // Given: User with empty watchlist
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "Empty Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Posting monitor request
        final MockHttpServletRequestBuilder requestBuilder = post(USER_MONITOR_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return empty channels list
        result.andExpect(status().isOk());

        final MonitorResponse response = fromJson(
            result.andReturn().getResponse().getContentAsString(),
            MonitorResponse.class
        );

        assertThat(response.getDashboard().getChannels(), is(empty()));
    }

    // ========================= VALIDATION FAILURES =========================

    @Test
    @DisplayName("Should fail when watchlistId is missing")
    void shouldFailWhenWatchlistIdIsMissing() throws Exception {
        // Given: Request without watchlistId
        final User user = aValidatedUser();
        final MonitorRequest request = new MonitorRequest();
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Posting monitor request
        final MockHttpServletRequestBuilder requestBuilder = post(USER_MONITOR_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return forbidden (PreAuthorize fails before validation)
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail when both timeRange and custom range provided")
    void shouldFailWhenBothTimeRangeAndCustomRangeProvided() throws Exception {
        // Given: Request with both timeRange and custom range
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .startTime(OffsetDateTime.now().minusDays(1))
                .endTime(OffsetDateTime.now())
                .build()
        );

        // When: Posting monitor request
        final MockHttpServletRequestBuilder requestBuilder = post(USER_MONITOR_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return bad request
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail when endTime before startTime")
    void shouldFailWhenEndTimeBeforeStartTime() throws Exception {
        // Given: Request with invalid date range
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .startTime(OffsetDateTime.now())
                .endTime(OffsetDateTime.now().minusDays(1))
                .build()
        );

        // When: Posting monitor request
        final MockHttpServletRequestBuilder requestBuilder = post(USER_MONITOR_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return bad request
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail when custom range exceeds 30 days")
    void shouldFailWhenCustomRangeExceeds30Days() throws Exception {
        // Given: Request with range > 30 days
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .startTime(OffsetDateTime.now().minusDays(31))
                .endTime(OffsetDateTime.now())
                .build()
        );

        // When: Posting monitor request
        final MockHttpServletRequestBuilder requestBuilder = post(USER_MONITOR_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return bad request
        result.andExpect(status().isBadRequest());
    }

    // ========================= AUTHORIZATION FAILURES =========================

    @Test
    @DisplayName("Should fail when watchlist not found")
    void shouldFailWhenWatchlistNotFound() throws Exception {
        // Given: Request for non-existent watchlist
        final User user = aValidatedUser();
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(999L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Posting monitor request
        final MockHttpServletRequestBuilder requestBuilder = post(USER_MONITOR_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return forbidden (PreAuthorize fails on non-existent watchlist)
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail when user does not own watchlist")
    void shouldFailWhenUserDoesNotOwnWatchlist() throws Exception {
        // Given: Request for watchlist owned by different user
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user2, "Other User's Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: User1 tries to access user2's watchlist
        final MockHttpServletRequestBuilder requestBuilder = post(USER_MONITOR_PATH)
            .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return forbidden (PreAuthorize denies access)
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail when not authenticated")
    void shouldFailWhenNotAuthenticated() throws Exception {
        // Given: Request without authentication
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Posting monitor request without auth header
        final MockHttpServletRequestBuilder requestBuilder = post(USER_MONITOR_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return unauthorized
        result.andExpect(status().isUnauthorized());
    }
}
