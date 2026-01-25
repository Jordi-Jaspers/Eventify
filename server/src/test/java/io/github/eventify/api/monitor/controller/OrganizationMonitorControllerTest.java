package io.github.eventify.api.monitor.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.monitor.model.request.MonitorRequest;
import io.github.eventify.api.monitor.model.response.MonitorResponse;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.support.IntegrationTest;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ORGANIZATION_MONITOR_PATH;
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
 * Integration tests for OrganizationMonitorController.
 */
@DisplayName("Integration Test - Organization Monitor Controller")
class OrganizationMonitorControllerTest extends IntegrationTest {

    // ========================= SUCCESSFUL REQUESTS =========================

    @Test
    @DisplayName("Should return monitor data when user is org owner")
    void shouldReturnMonitorDataWhenUserIsOrgOwner() throws Exception {
        // Given: Org owner with watchlist containing channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");
        final Channel channel = aChannelForOrganisation(owner, org, "org-channel");
        addChannelToWatchlist(watchlist, channel);
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(1));

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Posting organization monitor request
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
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
        assertThat(response.getWatchlistName(), is(equalTo("Org Watchlist")));
        assertThat(response.getConfiguration().getChannels(), hasSize(1));
        assertThat(response.isLive(), is(true));
    }

    @Test
    @DisplayName("Should return monitor data when user is org member")
    void shouldReturnMonitorDataWhenUserIsOrgMember() throws Exception {
        // Given: Org member with access to watchlist
        final User owner = aValidatedUser();
        final User member = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");
        final Channel channel = aChannelForOrganisation(owner, org, "org-channel");
        addChannelToWatchlist(watchlist, channel);

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_7D)
                .build()
        );

        // When: Member requests monitor data
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return monitor response
        result.andExpect(status().isOk());

        final MonitorResponse response = fromJson(
            result.andReturn().getResponse().getContentAsString(),
            MonitorResponse.class
        );

        assertThat(response.getWatchlistId(), is(equalTo(watchlist.getId())));
    }

    @Test
    @DisplayName("Should handle paused channels in organization")
    void shouldHandlePausedChannelsInOrganization() throws Exception {
        // Given: Org watchlist with paused channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");
        final Channel channel = aChannelForOrganisation(owner, org, "paused-channel");
        pauseChannel(channel);
        addChannelToWatchlist(watchlist, channel);

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Posting organization monitor request
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return paused channel
        result.andExpect(status().isOk());

        final MonitorResponse response = fromJson(
            result.andReturn().getResponse().getContentAsString(),
            MonitorResponse.class
        );

        assertThat(response.getConfiguration().getChannels(), hasSize(1));
        assertThat(response.getConfiguration().getChannels().get(0).getStatus().name(), is(equalTo("PAUSED")));
    }

    @Test
    @DisplayName("Should sort channels by severity when requested")
    void shouldSortChannelsBySeverityWhenRequested() throws Exception {
        // Given: Org watchlist with multiple severity channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");

        final Channel okChannel = aChannelForOrganisation(owner, org, "ok-channel");
        final Channel warningChannel = aChannelForOrganisation(owner, org, "warning-channel");
        final Channel criticalChannel = aChannelForOrganisation(owner, org, "critical-channel");

        addChannelToWatchlist(watchlist, okChannel);
        addChannelToWatchlist(watchlist, warningChannel);
        addChannelToWatchlist(watchlist, criticalChannel);

        final OffsetDateTime eventTime = OffsetDateTime.now().minusHours(1);
        anEventForChannel(okChannel, Severity.OK, eventTime);
        anEventForChannel(warningChannel, Severity.WARNING, eventTime);
        anEventForChannel(criticalChannel, Severity.CRITICAL, eventTime);

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .sortBySeverity(true)
                .build()
        );

        // When: Posting organization monitor request
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should sort by severity (CRITICAL, WARNING, OK)
        result.andExpect(status().isOk());

        final MonitorResponse response = fromJson(
            result.andReturn().getResponse().getContentAsString(),
            MonitorResponse.class
        );

        assertThat(response.getConfiguration().getChannels(), hasSize(3));
        assertThat(response.getConfiguration().getChannels().get(0).getChannelName(), is(equalTo("critical-channel")));
        assertThat(response.getConfiguration().getChannels().get(1).getChannelName(), is(equalTo("warning-channel")));
        assertThat(response.getConfiguration().getChannels().get(2).getChannelName(), is(equalTo("ok-channel")));
    }

    // ========================= VALIDATION FAILURES =========================

    @Test
    @DisplayName("Should fail when watchlistId is missing")
    void shouldFailWhenWatchlistIdIsMissing() throws Exception {
        // Given: Request without watchlistId
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final MonitorRequest request = new MonitorRequest();
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Posting organization monitor request
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return forbidden (PreAuthorize fails before validation)
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail when only startTime provided")
    void shouldFailWhenOnlyStartTimeProvided() throws Exception {
        // Given: Request with only startTime
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .startTime(OffsetDateTime.now().minusDays(1))
                .build()
        );

        // When: Posting organization monitor request
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return bad request
        result.andExpect(status().isBadRequest());
    }

    // ========================= AUTHORIZATION FAILURES =========================

    @Test
    @DisplayName("Should fail when user not org member")
    void shouldFailWhenUserNotOrgMember() throws Exception {
        // Given: Non-member trying to access org watchlist
        final User owner = aValidatedUser();
        final User nonMember = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Non-member requests monitor data
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return not found
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail when watchlist not in organization")
    void shouldFailWhenWatchlistNotInOrganization() throws Exception {
        // Given: Watchlist in different organization
        final User owner = aValidatedUser();
        final Organization org1 = anOrganisationWithOwner(owner);
        final Organization org2 = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org2, "Org2 Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Requesting from wrong organization
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", org1.getId().toString());
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return not found
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail when watchlist is personal not org")
    void shouldFailWhenWatchlistIsPersonalNotOrg() throws Exception {
        // Given: Personal watchlist being accessed via org endpoint
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist personalWatchlist = aWatchlistForUser(owner, "Personal Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(personalWatchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Requesting via organization endpoint
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return not found
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail when watchlist not found")
    void shouldFailWhenWatchlistNotFound() throws Exception {
        // Given: Request for non-existent watchlist
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(999L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Posting organization monitor request
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return not found
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail when not authenticated")
    void shouldFailWhenNotAuthenticated() throws Exception {
        // Given: Request without authentication
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Watchlist watchlist = aWatchlistForOrganization(owner, org, "Org Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Posting organization monitor request without auth
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return unauthorized
        result.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail when organization does not exist")
    void shouldFailWhenOrganizationDoesNotExist() throws Exception {
        // Given: Request for non-existent organization
        final User user = aValidatedUser();
        final Watchlist watchlist = aWatchlistForUser(user, "My Watchlist");

        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(watchlist.getId());
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Posting to non-existent organization
        final String path = ORGANIZATION_MONITOR_PATH.replace("{orgId}", "999");
        final MockHttpServletRequestBuilder requestBuilder = post(path)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions result = mockMvc.perform(requestBuilder);

        // Then: Should return not found
        result.andExpect(status().isForbidden());
    }
}
