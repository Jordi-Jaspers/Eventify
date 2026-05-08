package io.github.eventify.api.dashboard.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.dashboard.model.response.DashboardStatsResponse;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.util.TimeProvider;
import io.github.eventify.support.IntegrationTest;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ORGANIZATION_DASHBOARD_STATS_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Organization Dashboard Controller")
public class OrganizationDashboardControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return org stats when member requests")
    public void getOrgStatsSuccess() throws Exception {
        // Given: An organization with a member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Org has channels
        aChannelForOrganisation(owner, org, "Org Channel 1");
        aChannelForOrganisation(owner, org, "Org Channel 2");

        // When: Member requests org dashboard stats
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

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
    @DisplayName("Should reject non-member access with 403")
    public void getOrgStatsNonMemberFails() throws Exception {
        // Given: An organization with a member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Another user who is NOT a member
        final User nonMember = aValidatedUser();

        // When: Non-member requests org dashboard stats
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should include only org channels in stats")
    public void getOrgStatsShouldExcludePersonalChannels() throws Exception {
        // Given: An organization with a member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: 3 org channels and 2 personal channels
        aChannelForOrganisation(owner, org, "Org Channel 1");
        aChannelForOrganisation(owner, org, "Org Channel 2");
        aChannelForOrganisation(owner, org, "Org Channel 3");
        aChannelForUser(owner, "Personal Channel 1");
        aChannelForUser(owner, "Personal Channel 2");

        // When: Requesting org dashboard stats
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Should only count org channels
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.activeChannels(), is(equalTo(3)));
    }

    @Test
    @DisplayName("Should allow admin role access")
    public void getOrgStatsAdminSuccess() throws Exception {
        // Given: An organization with owner and admin
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);

        // And: Org has channels
        aChannelForOrganisation(owner, org, "Org Channel 1");

        // When: Admin requests org dashboard stats
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats, is(notNullValue()));
    }

    @Test
    @DisplayName("Should allow member role access")
    public void getOrgStatsMemberSuccess() throws Exception {
        // Given: An organization with owner and regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Org has channels
        aChannelForOrganisation(owner, org, "Org Channel 1");

        // When: Member requests org dashboard stats
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats, is(notNullValue()));
    }

    @Test
    @DisplayName("Should calculate error rate correctly for org channels")
    public void getOrgStatsShouldCalculateErrorRateCorrectly() throws Exception {
        // Given: An organization with channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: 5 channels, 2 with CRITICAL events (40% error rate)
        final Channel channel1 = aChannelForOrganisation(owner, org, "Channel 1");
        final Channel channel2 = aChannelForOrganisation(owner, org, "Channel 2");
        final Channel channel3 = aChannelForOrganisation(owner, org, "Channel 3");
        final Channel channel4 = aChannelForOrganisation(owner, org, "Channel 4");
        final Channel channel5 = aChannelForOrganisation(owner, org, "Channel 5");

        anEventForChannel(channel1, Severity.CRITICAL, OffsetDateTime.now());
        anEventForChannel(channel2, Severity.CRITICAL, OffsetDateTime.now());
        anEventForChannel(channel3, Severity.OK, OffsetDateTime.now());
        anEventForChannel(channel4, Severity.OK, OffsetDateTime.now());
        anEventForChannel(channel5, Severity.WARNING, OffsetDateTime.now());

        // When: Requesting org dashboard stats
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Error rate should be 40.0%
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.errorRate(), is(equalTo(40.0)));
    }

    @Test
    @DisplayName("Should return zero stats when org has no channels")
    public void getOrgStatsShouldReturnZeroWhenNoChannels() throws Exception {
        // Given: An organization with no channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting org dashboard stats
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

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
    @DisplayName("Should count org events from last 24 hours only")
    public void getOrgStatsShouldCountRecentEventsOnly() throws Exception {
        // Given: An organization with a channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Channel channel = aChannelForOrganisation(owner, org, "Test Channel");

        // And: Events from different time periods
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(1));
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(12));
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(48));

        // When: Requesting org dashboard stats
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Should only count events from last 24 hours
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.eventsToday(), is(equalTo(2L)));
    }

    @Test
    @DisplayName("Should return most recent event timestamp for org")
    public void getOrgStatsShouldReturnMostRecentEventTimestamp() throws Exception {
        // Given: An organization with channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Channel channel1 = aChannelForOrganisation(owner, org, "Channel 1");
        final Channel channel2 = aChannelForOrganisation(owner, org, "Channel 2");

        // And: Events with different timestamps (use TimeProvider for microsecond precision)
        final OffsetDateTime older = TimeProvider.now().minusHours(5);
        final OffsetDateTime newer = TimeProvider.now().minusMinutes(10);

        anEventForChannel(channel1, Severity.OK, older);
        anEventForChannel(channel2, Severity.OK, newer);

        // When: Requesting org dashboard stats
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

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
    @DisplayName("Should reject unauthenticated request")
    public void getOrgStatsWithoutAuthFails() throws Exception {
        // Given: An organization
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting org dashboard stats without auth
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 404 when organization does not exist")
    public void getOrgStatsNonExistentOrgFails() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Non-existent organization ID
        final Long nonExistentOrgId = 99999L;

        // When: Requesting stats for non-existent org
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", nonExistentOrgId.toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be not found
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should exclude paused channels from org stats")
    public void getOrgStatsShouldExcludePausedChannels() throws Exception {
        // Given: An organization with active and paused channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        aChannelForOrganisation(owner, org, "Active Channel 1");
        aChannelForOrganisation(owner, org, "Active Channel 2");
        final Channel pausedChannel = aChannelForOrganisation(owner, org, "Paused Channel");
        pauseChannel(pausedChannel);

        // When: Requesting org dashboard stats
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Should only count active channels
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.activeChannels(), is(equalTo(2)));
    }

    @Test
    @DisplayName("Should isolate stats between different organizations")
    public void getOrgStatsShouldIsolateStatsAcrossOrgs() throws Exception {
        // Given: Two separate organizations
        final User owner1 = aValidatedUser();
        final User owner2 = aValidatedUser();
        final Organization org1 = anOrganisationWithOwner(owner1);
        final Organization org2 = anOrganisationWithOwner(owner2);

        // And: Each org has different channel counts
        aChannelForOrganisation(owner1, org1, "Org1 Channel 1");
        aChannelForOrganisation(owner1, org1, "Org1 Channel 2");
        aChannelForOrganisation(owner2, org2, "Org2 Channel 1");
        aChannelForOrganisation(owner2, org2, "Org2 Channel 2");
        aChannelForOrganisation(owner2, org2, "Org2 Channel 3");

        // When: Requesting stats for org1
        final MockHttpServletRequestBuilder request = get(ORGANIZATION_DASHBOARD_STATS_PATH.replace("{orgId}", org1.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner1.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Should only reflect org1's channels
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardStatsResponse stats = fromJson(content, DashboardStatsResponse.class);

        assertThat(stats.activeChannels(), is(equalTo(2)));
    }
}
