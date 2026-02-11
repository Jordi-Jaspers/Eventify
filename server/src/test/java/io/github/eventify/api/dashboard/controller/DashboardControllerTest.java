package io.github.eventify.api.dashboard.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.dashboard.model.response.DashboardStatsResponse;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.USER_DASHBOARD_STATS_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Dashboard Controller")
public class DashboardControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return personal stats when authenticated user requests")
    public void getPersonalStatsSuccess() throws Exception {
        // Given: An authenticated user with personal channels
        final User user = aValidatedUser();
        aChannelForUser(user, "Personal Channel 1");
        aChannelForUser(user, "Personal Channel 2");

        // When: Requesting personal dashboard stats
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain valid stats
        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats, is(notNullValue()));
        assertThat(stats.activeChannels(), is(equalTo(2)));
    }

    @Test
    @DisplayName("Should include only personal channels in stats")
    public void getPersonalStatsShouldExcludeOrgChannels() throws Exception {
        // Given: An authenticated user with personal and org channels
        final User user = aValidatedUser();
        final Organization org = anOrganisationWithOwner(user);

        // And: 2 personal channels and 3 org channels
        aChannelForUser(user, "Personal Channel 1");
        aChannelForUser(user, "Personal Channel 2");
        aChannelForOrganisation(user, org, "Org Channel 1");
        aChannelForOrganisation(user, org, "Org Channel 2");
        aChannelForOrganisation(user, org, "Org Channel 3");

        // When: Requesting personal dashboard stats
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Should only count personal channels
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.activeChannels(), is(equalTo(2)));
    }

    @Test
    @DisplayName("Should count events from last 24 hours only")
    public void getPersonalStatsShouldCountRecentEventsOnly() throws Exception {
        // Given: An authenticated user with a channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");

        // And: Events from different time periods
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(1));
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(12));
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(48));

        // When: Requesting personal dashboard stats
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Should only count events from last 24 hours
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.eventsToday(), is(equalTo(2L)));
    }

    @Test
    @DisplayName("Should calculate error rate correctly")
    public void getPersonalStatsShouldCalculateErrorRateCorrectly() throws Exception {
        // Given: An authenticated user with 5 channels
        final User user = aValidatedUser();
        final Channel channel1 = aChannelForUser(user, "Channel 1");
        final Channel channel2 = aChannelForUser(user, "Channel 2");
        final Channel channel3 = aChannelForUser(user, "Channel 3");
        final Channel channel4 = aChannelForUser(user, "Channel 4");
        final Channel channel5 = aChannelForUser(user, "Channel 5");

        // And: 2 channels with CRITICAL as last event (40% error rate)
        anEventForChannel(channel1, Severity.CRITICAL, OffsetDateTime.now());
        anEventForChannel(channel2, Severity.CRITICAL, OffsetDateTime.now());
        anEventForChannel(channel3, Severity.OK, OffsetDateTime.now());
        anEventForChannel(channel4, Severity.OK, OffsetDateTime.now());
        anEventForChannel(channel5, Severity.WARNING, OffsetDateTime.now());

        // When: Requesting personal dashboard stats
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Error rate should be 40.0%
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.errorRate(), is(equalTo(40.0)));
    }

    @Test
    @DisplayName("Should return zero stats when user has no channels")
    public void getPersonalStatsShouldReturnZeroWhenNoChannels() throws Exception {
        // Given: An authenticated user with no channels
        final User user = aValidatedUser();

        // When: Requesting personal dashboard stats
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: All stats should be zero
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.eventsToday(), is(equalTo(0L)));
        assertThat(stats.activeChannels(), is(equalTo(0)));
        assertThat(stats.errorRate(), is(equalTo(0.0)));
        assertThat(stats.lastEventAt(), is(nullValue()));
    }

    @Test
    @DisplayName("Should return most recent event timestamp")
    public void getPersonalStatsShouldReturnMostRecentEventTimestamp() throws Exception {
        // Given: An authenticated user with channels
        final User user = aValidatedUser();
        final Channel channel1 = aChannelForUser(user, "Channel 1");
        final Channel channel2 = aChannelForUser(user, "Channel 2");

        // And: Events with different timestamps
        final OffsetDateTime older = OffsetDateTime.now().minusHours(5);
        final OffsetDateTime newer = OffsetDateTime.now().minusMinutes(10);

        anEventForChannel(channel1, Severity.OK, older);
        anEventForChannel(channel2, Severity.OK, newer);

        // When: Requesting personal dashboard stats
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Last event timestamp should be the most recent
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.lastEventAt(), is(notNullValue()));
        // Compare instants since PostgreSQL normalizes TIMESTAMPTZ to UTC
        assertThat(stats.lastEventAt().toInstant(), is(equalTo(newer.toInstant())));
    }

    @Test
    @DisplayName("Should return null for last event when no events exist")
    public void getPersonalStatsShouldReturnNullLastEventWhenNoEvents() throws Exception {
        // Given: An authenticated user with channels but no events
        final User user = aValidatedUser();
        aChannelForUser(user, "Channel 1");

        // When: Requesting personal dashboard stats
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Last event timestamp should be null
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.lastEventAt(), is(nullValue()));
    }

    @Test
    @DisplayName("Should reject unauthenticated request")
    public void getPersonalStatsWithoutAuthFails() throws Exception {
        // Given: No authentication

        // When: Requesting personal dashboard stats without auth
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_STATS_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should exclude paused channels from active count")
    public void getPersonalStatsShouldExcludePausedChannels() throws Exception {
        // Given: An authenticated user with active and paused channels
        final User user = aValidatedUser();
        aChannelForUser(user, "Active Channel 1");
        aChannelForUser(user, "Active Channel 2");
        final Channel pausedChannel = aChannelForUser(user, "Paused Channel");
        pauseChannel(pausedChannel);

        // When: Requesting personal dashboard stats
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Should only count active channels
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.activeChannels(), is(equalTo(2)));
    }

    @Test
    @DisplayName("Should handle channels with multiple events correctly")
    public void getPersonalStatsShouldHandleMultipleEventsPerChannel() throws Exception {
        // Given: An authenticated user with a channel having multiple events
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");

        // And: Multiple events with CRITICAL as the most recent
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(5));
        anEventForChannel(channel, Severity.WARNING, OffsetDateTime.now().minusHours(3));
        anEventForChannel(channel, Severity.CRITICAL, OffsetDateTime.now().minusHours(1));

        // When: Requesting personal dashboard stats
        final MockHttpServletRequestBuilder request = get(USER_DASHBOARD_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Error rate should be 100% (only most recent event matters)
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.errorRate(), is(equalTo(100.0)));
        assertThat(stats.eventsToday(), is(equalTo(3L)));
    }
}
