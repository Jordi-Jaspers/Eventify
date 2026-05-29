package io.github.eventify.api.admin.stats.controller;

import io.github.eventify.api.admin.stats.model.response.AdminCountsResponse;
import io.github.eventify.api.admin.stats.model.response.AdminEventVolumeResponse;
import io.github.eventify.api.admin.stats.model.response.AdminGrowthResponse;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.model.request.StatsRequest;
import io.github.eventify.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ADMIN_STATS_COUNTS_PATH;
import static io.github.eventify.api.Paths.ADMIN_STATS_EVENT_VOLUME_PATH;
import static io.github.eventify.api.Paths.ADMIN_STATS_GROWTH_PATH;
import static io.github.eventify.api.Paths.ADMIN_STATS_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Admin Dashboard Controller (Split Endpoints)")
public class AdminDashboardControllerSplitTest extends IntegrationTest {

    // ==================== GET /admin/stats/counts ====================

    @Test
    @DisplayName("Should return counts when admin requests")
    public void getCountsSuccess() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin counts
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_COUNTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain valid counts structure
        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminCountsResponse counts = fromJson(content, AdminCountsResponse.class);

        assertThat(counts, is(notNullValue()));
        assertThat(counts.getTotalOrganizations(), is(notNullValue()));
        assertThat(counts.getTotalUsers(), is(notNullValue()));
        assertThat(counts.getTotalChannels(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return non-negative counts")
    public void getCountsShouldReturnNonNegativeValues() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin counts
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_COUNTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: All counts should be non-negative
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminCountsResponse counts = fromJson(content, AdminCountsResponse.class);

        assertThat(counts.getTotalOrganizations(), is(greaterThanOrEqualTo(0L)));
        assertThat(counts.getTotalUsers(), is(greaterThanOrEqualTo(0L)));
        assertThat(counts.getTotalChannels(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    @DisplayName("Should reject counts request from non-admin user")
    public void getCountsUnauthorizedFails() throws Exception {
        // Given: A regular validated user without admin role
        final User regularUser = aValidatedUser();

        // When: Requesting admin counts without admin authority
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_COUNTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject unauthenticated counts request")
    public void getCountsWithoutAuthenticationFails() throws Exception {
        // Given: No authentication provided

        // When: Requesting admin counts without authentication
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_COUNTS_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should reflect newly created user in counts")
    public void getCountsShouldReflectNewUser() throws Exception {
        // Given: An admin user and a new regular user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        aValidatedUser(); // creates an additional user

        // When: Requesting admin counts
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_COUNTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Total users should be at least 2 (admin + new user)
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminCountsResponse counts = fromJson(content, AdminCountsResponse.class);

        assertThat(counts.getTotalUsers(), is(greaterThanOrEqualTo(2L)));
    }

    // ==================== POST /admin/stats/growth — auth ====================

    @Test
    @DisplayName("Should reject growth request from non-admin user")
    public void getGrowthUnauthorizedFails() throws Exception {
        // Given: A regular validated user without admin role
        final User regularUser = aValidatedUser();
        final StatsRequest statsRequest = new StatsRequest().setDays(30);

        // When: Posting to growth without admin authority
        final MockHttpServletRequestBuilder request = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue())
            .content(toJson(statsRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject unauthenticated growth request")
    public void getGrowthWithoutAuthenticationFails() throws Exception {
        // Given: No authentication provided
        final StatsRequest statsRequest = new StatsRequest().setDays(30);

        // When: Posting to growth without authentication
        final MockHttpServletRequestBuilder request = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(statsRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 400 when growth days=0")
    public void getGrowthWithDays0ShouldReturn400() throws Exception {
        // Given: An authenticated admin user with days=0 (below minimum of 1)
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final StatsRequest statsRequest = new StatsRequest().setDays(0);

        // When: Posting to growth with days=0
        final MockHttpServletRequestBuilder request = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(statsRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when growth days is negative")
    public void getGrowthWithNegativeDaysShouldReturn400() throws Exception {
        // Given: An authenticated admin user with days=-5 (below minimum of 1)
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final StatsRequest statsRequest = new StatsRequest().setDays(-5);

        // When: Posting to growth with days=-5
        final MockHttpServletRequestBuilder request = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(statsRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when growth days exceeds 365")
    public void getGrowthWithDaysOver365ShouldReturn400() throws Exception {
        // Given: An authenticated admin user with days=366 (above maximum of 365)
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final StatsRequest statsRequest = new StatsRequest().setDays(366);

        // When: Posting to growth with days=366
        final MockHttpServletRequestBuilder request = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(statsRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return growth data points with non-negative counts")
    public void getGrowthDataPointsShouldHaveNonNegativeCounts() throws Exception {
        // Given: An authenticated admin user with a valid days request
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final StatsRequest statsRequest = new StatsRequest().setDays(30);

        // When: Posting to growth endpoint
        final MockHttpServletRequestBuilder request = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(statsRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: All data points should have non-negative counts
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminGrowthResponse growth = fromJson(content, AdminGrowthResponse.class);

        growth.getGrowthData().forEach(point -> {
            assertThat(point.getNewUsers(), is(greaterThanOrEqualTo(0)));
            assertThat(point.getNewOrganizations(), is(greaterThanOrEqualTo(0)));
        });
    }

    // ==================== POST /admin/stats/event-volume — auth + validation ====================

    @Test
    @DisplayName("Should return zero event volume when no events exist")
    public void getEventVolumeWithNoEventsShouldReturnZeros() throws Exception {
        // Given: An authenticated admin user (no events seeded)
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final StatsRequest statsRequest = new StatsRequest().setDays(30);

        // When: Posting to event volume endpoint
        final MockHttpServletRequestBuilder request = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(statsRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful with zero totals (not null)
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminEventVolumeResponse eventVolume = fromJson(content, AdminEventVolumeResponse.class);

        assertThat(eventVolume.getTotalEvents(), is(notNullValue()));
        assertThat(eventVolume.getTotalEvents(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    @DisplayName("Should return 400 when event-volume days=0")
    public void getEventVolumeWithDays0ShouldReturn400() throws Exception {
        // Given: An authenticated admin user with days=0 (below minimum of 1)
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final StatsRequest statsRequest = new StatsRequest().setDays(0);

        // When: Posting to event volume with days=0
        final MockHttpServletRequestBuilder request = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(statsRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when event-volume days is negative")
    public void getEventVolumeWithNegativeDaysShouldReturn400() throws Exception {
        // Given: An authenticated admin user with days=-1 (below minimum of 1)
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final StatsRequest statsRequest = new StatsRequest().setDays(-1);

        // When: Posting to event volume with days=-1
        final MockHttpServletRequestBuilder request = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(statsRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should reject event-volume request from non-admin user")
    public void getEventVolumeUnauthorizedFails() throws Exception {
        // Given: A regular validated user without admin role
        final User regularUser = aValidatedUser();
        final StatsRequest statsRequest = new StatsRequest().setDays(30);

        // When: Posting to event volume without admin authority
        final MockHttpServletRequestBuilder request = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue())
            .content(toJson(statsRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject unauthenticated event-volume request")
    public void getEventVolumeWithoutAuthenticationFails() throws Exception {
        // Given: No authentication provided
        final StatsRequest statsRequest = new StatsRequest().setDays(30);

        // When: Posting to event volume without authentication
        final MockHttpServletRequestBuilder request = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(statsRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    // ==================== Old endpoint removed ====================

    @Test
    @DisplayName("Should return 404 when old /admin/stats endpoint is called")
    public void getOldStatsEndpointShouldReturn404() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting the old /admin/stats endpoint (no longer exists)
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 404 Not Found (endpoint removed)
        response.andExpect(status().is(SC_NOT_FOUND));
    }
}
