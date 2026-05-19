package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.response.AdminCountsResponse;
import io.github.eventify.api.admin.model.response.AdminEventVolumeResponse;
import io.github.eventify.api.admin.model.response.AdminGrowthResponse;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
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
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    // ==================== GET /admin/stats/growth ====================

    @Test
    @DisplayName("Should return growth data when admin requests with default days")
    public void getGrowthSuccess() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin growth without days param (defaults to 30)
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain growth data
        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminGrowthResponse growth = fromJson(content, AdminGrowthResponse.class);

        assertThat(growth, is(notNullValue()));
        assertThat(growth.getGrowthData(), is(notNullValue()));
        assertThat(growth.getGrowthData(), hasSize(31)); // 30 days + today
    }

    @Test
    @DisplayName("Should return 7-day growth data when days=7")
    public void getGrowthWithDays7ShouldReturn8DataPoints() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting growth with days=7
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_GROWTH_PATH)
            .param("days", "7")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should contain 8 data points (7 days + today)
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminGrowthResponse growth = fromJson(content, AdminGrowthResponse.class);

        assertThat(growth.getGrowthData(), hasSize(8));
    }

    @Test
    @DisplayName("Should return 90-day growth data when days=90")
    public void getGrowthWithDays90ShouldReturn91DataPoints() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting growth with days=90
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_GROWTH_PATH)
            .param("days", "90")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should contain 91 data points
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminGrowthResponse growth = fromJson(content, AdminGrowthResponse.class);

        assertThat(growth.getGrowthData(), hasSize(91));
    }

    @Test
    @DisplayName("Should return 400 when growth days=365 (not an allowed value)")
    public void getGrowthWithDays365ShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting growth with days=365 (not in allowed set 7/30/90/180)
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_GROWTH_PATH)
            .param("days", "365")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when growth days=0")
    public void getGrowthWithDays0ShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting growth with days=0
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_GROWTH_PATH)
            .param("days", "0")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when growth days is negative")
    public void getGrowthWithNegativeDaysShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting growth with days=-5
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_GROWTH_PATH)
            .param("days", "-5")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should reject growth request from non-admin user")
    public void getGrowthUnauthorizedFails() throws Exception {
        // Given: A regular validated user without admin role
        final User regularUser = aValidatedUser();

        // When: Requesting growth without admin authority
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject unauthenticated growth request")
    public void getGrowthWithoutAuthenticationFails() throws Exception {
        // Given: No authentication provided

        // When: Requesting growth without authentication
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return growth data points with non-negative counts")
    public void getGrowthDataPointsShouldHaveNonNegativeCounts() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting growth
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

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

    // ==================== GET /admin/stats/event-volume ====================

    @Test
    @DisplayName("Should return event volume when admin requests with default days")
    public void getEventVolumeSuccess() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event volume without days param (defaults to 30)
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain event volume data
        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminEventVolumeResponse eventVolume = fromJson(content, AdminEventVolumeResponse.class);

        assertThat(eventVolume, is(notNullValue()));
        assertThat(eventVolume.getDailyVolume(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return 400 when event-volume days=0")
    public void getEventVolumeWithDays0ShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event volume with days=0
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENT_VOLUME_PATH)
            .param("days", "0")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when event-volume days is negative")
    public void getEventVolumeWithNegativeDaysShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event volume with days=-1
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENT_VOLUME_PATH)
            .param("days", "-1")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when event-volume days=365 (not an allowed value)")
    public void getEventVolumeWithDays365ShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event volume with days=365 (not in allowed set 7/30/90/180)
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENT_VOLUME_PATH)
            .param("days", "365")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return zero event volume when no events exist")
    public void getEventVolumeWithNoEventsShouldReturnZeros() throws Exception {
        // Given: An authenticated admin user (no events seeded)
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event volume
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful with zero totals (not null)
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminEventVolumeResponse eventVolume = fromJson(content, AdminEventVolumeResponse.class);

        assertThat(eventVolume.getTotalEvents(), is(notNullValue()));
        assertThat(eventVolume.getTotalEvents(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    @DisplayName("Should reject event-volume request from non-admin user")
    public void getEventVolumeUnauthorizedFails() throws Exception {
        // Given: A regular validated user without admin role
        final User regularUser = aValidatedUser();

        // When: Requesting event volume without admin authority
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject unauthenticated event-volume request")
    public void getEventVolumeWithoutAuthenticationFails() throws Exception {
        // Given: No authentication provided

        // When: Requesting event volume without authentication
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON);

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
