package io.github.eventify.api.organization.controller;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.response.OrgApiKeyStatsResponse;
import io.github.eventify.api.organization.model.response.OrgSummaryResponse;
import io.github.eventify.api.organization.model.response.OrgTimelineResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ORGANIZATION_STATS_API_KEYS_PATH;
import static io.github.eventify.api.Paths.ORGANIZATION_STATS_SUMMARY_PATH;
import static io.github.eventify.api.Paths.ORGANIZATION_STATS_TIMELINE_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Org Statistics Controller")
public class OrgStatisticsControllerTest extends IntegrationTest {

    private String timelinePath(final Long orgId) {
        return ORGANIZATION_STATS_TIMELINE_PATH.replace("{orgId}", orgId.toString());
    }

    private String summaryPath(final Long orgId) {
        return ORGANIZATION_STATS_SUMMARY_PATH.replace("{orgId}", orgId.toString());
    }

    private String apiKeysPath(final Long orgId) {
        return ORGANIZATION_STATS_API_KEYS_PATH.replace("{orgId}", orgId.toString());
    }

    // ─── GET /stats/timeline ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return timeline for organization owner")
    public void getTimelineAsOwner() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Owner requests timeline
        final MockHttpServletRequestBuilder request = get(timelinePath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK with valid structure
        response.andExpect(status().is(SC_OK));

        final OrgTimelineResponse result = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            OrgTimelineResponse.class
        );
        assertThat(result, is(notNullValue()));
        assertThat(result.getTimeline(), is(notNullValue()));
        assertThat(result.getErrorTimeline(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return timeline for organization admin")
    public void getTimelineAsAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);

        // When: Admin requests timeline
        final MockHttpServletRequestBuilder request = get(timelinePath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return forbidden when member requests timeline")
    public void getTimelineAsMemberForbidden() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // When: Member requests timeline
        final MockHttpServletRequestBuilder request = get(timelinePath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return forbidden when non-member requests timeline")
    public void getTimelineAsNonMemberForbidden() throws Exception {
        // Given: An organization and a user who is not a member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User nonMember = aValidatedUser();

        // When: Non-member requests timeline
        final MockHttpServletRequestBuilder request = get(timelinePath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return unauthorized when unauthenticated requests timeline")
    public void getTimelineUnauthorized() throws Exception {
        // Given: An organization
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting timeline without auth
        final MockHttpServletRequestBuilder request = get(timelinePath(org.getId()))
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return bad request when timeline days is 0")
    public void getTimelineWithZeroDaysBadRequest() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting timeline with days=0
        final MockHttpServletRequestBuilder request = get(timelinePath(org.getId()))
            .param("days", "0")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return bad request when timeline days is negative")
    public void getTimelineWithNegativeDaysBadRequest() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting timeline with days=-1
        final MockHttpServletRequestBuilder request = get(timelinePath(org.getId()))
            .param("days", "-1")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return bad request when timeline days is 999 (above maximum)")
    public void getTimelineWithDays999BadRequest() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting timeline with days=999
        final MockHttpServletRequestBuilder request = get(timelinePath(org.getId()))
            .param("days", "999")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return timeline with days=1 (minimum boundary)")
    public void getTimelineWithMinimumDays() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting timeline with days=1
        final MockHttpServletRequestBuilder request = get(timelinePath(org.getId()))
            .param("days", "1")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return timeline with days=365 (maximum boundary)")
    public void getTimelineWithMaximumDays() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting timeline with days=365
        final MockHttpServletRequestBuilder request = get(timelinePath(org.getId()))
            .param("days", "365")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return empty timeline when org has no channels")
    public void getTimelineEmptyWhenNoChannels() throws Exception {
        // Given: An organization with owner but no channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Owner requests timeline
        final MockHttpServletRequestBuilder request = get(timelinePath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK with empty timelines
        response.andExpect(status().is(SC_OK));

        final OrgTimelineResponse result = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            OrgTimelineResponse.class
        );
        assertThat(result.getTimeline(), is(empty()));
        assertThat(result.getErrorTimeline(), is(empty()));
    }

    // ─── GET /stats/summary ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return summary for organization owner")
    public void getSummaryAsOwner() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Owner requests summary
        final MockHttpServletRequestBuilder request = get(summaryPath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK with valid structure
        response.andExpect(status().is(SC_OK));

        final OrgSummaryResponse result = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            OrgSummaryResponse.class
        );
        assertThat(result, is(notNullValue()));
        assertThat(result.getTotalEvents(), is(greaterThanOrEqualTo(0L)));
        assertThat(result.getTotalChannels(), is(greaterThanOrEqualTo(0L)));
        assertThat(result.getAvgDailyVolume(), is(greaterThanOrEqualTo(0L)));
        assertThat(result.getCurrentErrorRate(), is(greaterThanOrEqualTo(0.0)));
    }

    @Test
    @DisplayName("Should return summary for organization admin")
    public void getSummaryAsAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);

        // When: Admin requests summary
        final MockHttpServletRequestBuilder request = get(summaryPath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return forbidden when member requests summary")
    public void getSummaryAsMemberForbidden() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // When: Member requests summary
        final MockHttpServletRequestBuilder request = get(summaryPath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return forbidden when non-member requests summary")
    public void getSummaryAsNonMemberForbidden() throws Exception {
        // Given: An organization and a user who is not a member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User nonMember = aValidatedUser();

        // When: Non-member requests summary
        final MockHttpServletRequestBuilder request = get(summaryPath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return unauthorized when unauthenticated requests summary")
    public void getSummaryUnauthorized() throws Exception {
        // Given: An organization
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting summary without auth
        final MockHttpServletRequestBuilder request = get(summaryPath(org.getId()))
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return bad request when summary days is 0")
    public void getSummaryWithZeroDaysBadRequest() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting summary with days=0
        final MockHttpServletRequestBuilder request = get(summaryPath(org.getId()))
            .param("days", "0")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return bad request when summary days is negative")
    public void getSummaryWithNegativeDaysBadRequest() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting summary with days=-1
        final MockHttpServletRequestBuilder request = get(summaryPath(org.getId()))
            .param("days", "-1")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return bad request when summary days is 999 (above maximum)")
    public void getSummaryWithDays999BadRequest() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting summary with days=999
        final MockHttpServletRequestBuilder request = get(summaryPath(org.getId()))
            .param("days", "999")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return summary with days=1 (minimum boundary)")
    public void getSummaryWithMinimumDays() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting summary with days=1
        final MockHttpServletRequestBuilder request = get(summaryPath(org.getId()))
            .param("days", "1")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return summary with days=365 (maximum boundary)")
    public void getSummaryWithMaximumDays() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting summary with days=365
        final MockHttpServletRequestBuilder request = get(summaryPath(org.getId()))
            .param("days", "365")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return summary with totalChannels when org has channels")
    public void getSummaryWithChannelsCount() throws Exception {
        // Given: An organization with owner and channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        aChannelForOrganisation(owner, org, "Channel One");
        aChannelForOrganisation(owner, org, "Channel Two");

        // When: Owner requests summary
        final MockHttpServletRequestBuilder request = get(summaryPath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK with correct channel count
        response.andExpect(status().is(SC_OK));

        final OrgSummaryResponse result = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            OrgSummaryResponse.class
        );
        assertThat(result.getTotalChannels(), is(equalTo(2L)));
    }

    // ─── GET /stats/api-keys ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return api-keys stats for organization owner")
    public void getApiKeyStatsAsOwner() throws Exception {
        // Given: An organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Owner requests api-keys stats
        final MockHttpServletRequestBuilder request = get(apiKeysPath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK with valid structure
        response.andExpect(status().is(SC_OK));

        final OrgApiKeyStatsResponse result = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            OrgApiKeyStatsResponse.class
        );
        assertThat(result, is(notNullValue()));
        assertThat(result.getNeverUsed(), is(greaterThanOrEqualTo(0L)));
        assertThat(result.getExpiringThisMonth(), is(greaterThanOrEqualTo(0L)));
        assertThat(result.getRevokedThisMonth(), is(greaterThanOrEqualTo(0L)));
        assertThat(result.getTopKeys(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return api-keys stats for organization admin")
    public void getApiKeyStatsAsAdmin() throws Exception {
        // Given: An organization with admin member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User admin = aValidatedUser();
        addMemberToOrganization(org, admin, OrganizationalRole.ADMIN);

        // When: Admin requests api-keys stats
        final MockHttpServletRequestBuilder request = get(apiKeysPath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return forbidden when member requests api-keys stats")
    public void getApiKeyStatsAsMemberForbidden() throws Exception {
        // Given: An organization with regular member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // When: Member requests api-keys stats
        final MockHttpServletRequestBuilder request = get(apiKeysPath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return forbidden when non-member requests api-keys stats")
    public void getApiKeyStatsAsNonMemberForbidden() throws Exception {
        // Given: An organization and a user who is not a member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final User nonMember = aValidatedUser();

        // When: Non-member requests api-keys stats
        final MockHttpServletRequestBuilder request = get(apiKeysPath(org.getId()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return unauthorized when unauthenticated requests api-keys stats")
    public void getApiKeyStatsUnauthorized() throws Exception {
        // Given: An organization
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // When: Requesting api-keys stats without auth
        final MockHttpServletRequestBuilder request = get(apiKeysPath(org.getId()))
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }
}
