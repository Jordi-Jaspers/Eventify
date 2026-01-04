package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.response.AdminStatsResponse;
import io.github.eventify.api.admin.model.response.GrowthDataPoint;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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

@DisplayName("Integration Test - Admin Dashboard Controller")
public class AdminDashboardControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return admin stats when authorized user requests")
    public void getStatsSuccess() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain valid stats structure
        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats, is(notNullValue()));
        assertThat(stats.getTotalOrganizations(), is(notNullValue()));
        assertThat(stats.getTotalUsers(), is(notNullValue()));
        assertThat(stats.getActiveUsers(), is(notNullValue()));
        assertThat(stats.getGrowthData(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return total organizations count in stats")
    public void getStatsShouldIncludeTotalOrganizationsCount() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should contain total organizations
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats.getTotalOrganizations(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    @DisplayName("Should return total users count in stats")
    public void getStatsShouldIncludeTotalUsersCount() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should contain total users
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats.getTotalUsers(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    @DisplayName("Should return active users count in stats")
    public void getStatsShouldIncludeActiveUsersCount() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should contain active users
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats.getActiveUsers(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    @DisplayName("Should return growth data in stats response")
    public void getStatsShouldIncludeGrowthData() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should contain growth data
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats.getGrowthData(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should reject request from non-admin user")
    public void getStatsUnauthorizedFails() throws Exception {
        // Given: A regular validated user without admin role
        final User regularUser = aValidatedUser();

        // When: Requesting admin stats without admin authority
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject unauthenticated request")
    public void getStatsWithoutAuthenticationFails() throws Exception {
        // Given: No authentication provided

        // When: Requesting admin stats without authentication
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return valid content type in response")
    public void getStatsShouldReturnApplicationJsonContentType() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should have application/json content type
        response.andExpect(status().is(SC_OK));

        final String contentType = response.andReturn().getResponse().getContentType();
        assertThat(contentType, containsString("application/json"));
    }

    @Test
    @DisplayName("Should maintain consistency where active users <= total users")
    public void getStatsShouldMaintainUserCountConsistency() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Active users should be <= total users
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats.getActiveUsers(), is(lessThanOrEqualTo(stats.getTotalUsers())));
    }

    @Test
    @DisplayName("Should return zero or positive counts for all statistics")
    public void getStatsShouldReturnNonNegativeCounts() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: All counts should be non-negative
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats.getTotalOrganizations(), is(greaterThanOrEqualTo(0L)));
        assertThat(stats.getTotalUsers(), is(greaterThanOrEqualTo(0L)));
        assertThat(stats.getActiveUsers(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    @DisplayName("Should return valid growth data points with proper structure")
    public void getStatsShouldReturnWellFormedGrowthData() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Growth data should be properly formatted
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats.getGrowthData(), is(notNullValue()));

        // And: Each growth data point should have required fields
        for (final GrowthDataPoint dataPoint : stats.getGrowthData()) {
            assertThat(dataPoint.getDate(), is(notNullValue()));
            assertThat(dataPoint.getNewOrganizations(), is(greaterThanOrEqualTo(0)));
            assertThat(dataPoint.getNewUsers(), is(greaterThanOrEqualTo(0)));
        }
    }

    @Test
    @DisplayName("Should handle empty database gracefully")
    public void getStatsShouldHandleEmptyDatabaseGracefully() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats on potentially empty database
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Should return valid response even with minimal data
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats, is(notNullValue()));
        assertThat(stats.getTotalOrganizations(), is(greaterThanOrEqualTo(0L)));
        assertThat(stats.getTotalUsers(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    @DisplayName("Should return growth data for last 30 days")
    public void getStatsShouldReturnGrowthDataForLast30Days() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        // And: Growth data dates should be within last 30 days
        final LocalDate today = LocalDate.now();
        final LocalDate thirtyDaysAgo = today.minusDays(30);

        for (final GrowthDataPoint dataPoint : stats.getGrowthData()) {
            assertThat(
                dataPoint.getDate(),
                allOf(
                    greaterThanOrEqualTo(thirtyDaysAgo),
                    lessThanOrEqualTo(today)
                )
            );
        }
    }

    @Test
    @DisplayName("Should reject request with invalid authorization token")
    public void getStatsWithInvalidTokenFails() throws Exception {
        // Given: Invalid authentication token

        // When: Requesting admin stats with invalid token
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + "invalid-token-12345");

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should allow admin user to access stats endpoint")
    public void getStatsWithAdminRoleShouldSucceed() throws Exception {
        // Given: Multiple admin users
        final User admin1 = aValidatedUserWithRole(Role.ADMIN);
        final User admin2 = aValidatedUserWithRole(Role.ADMIN);

        // When: Both admins request stats
        final MockHttpServletRequestBuilder request1 = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin1.getAccessToken().getValue());

        final MockHttpServletRequestBuilder request2 = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin2.getAccessToken().getValue());

        final ResultActions response1 = mockMvc.perform(request1);
        final ResultActions response2 = mockMvc.perform(request2);

        // Then: Both should return successful responses
        response1.andExpect(status().is(SC_OK));
        response2.andExpect(status().is(SC_OK));
    }
}
