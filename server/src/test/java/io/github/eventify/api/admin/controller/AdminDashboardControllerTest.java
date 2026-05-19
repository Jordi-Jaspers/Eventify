package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.response.AdminStatsResponse;
import io.github.eventify.api.admin.model.response.GrowthDataPoint;
import io.github.eventify.api.admin.model.response.TableSizeEntry;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import java.time.LocalDate;
import java.util.List;

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
    @DisplayName("Should return growth data for last 30 days by default")
    public void getStatsShouldReturnGrowthDataForLast30DaysByDefault() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats without days param (defaults to 30)
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

    // ==================== days param tests ====================

    @Test
    @DisplayName("Should return 7-day growth data when days=7")
    public void getStatsWithDays7ShouldReturn7DayWindow() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats with days=7
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .param("days", "7")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        // And: Growth data dates should be within last 7 days
        final LocalDate today = LocalDate.now();
        final LocalDate sevenDaysAgo = today.minusDays(7);

        for (final GrowthDataPoint dataPoint : stats.getGrowthData()) {
            assertThat(dataPoint.getDate(), greaterThanOrEqualTo(sevenDaysAgo));
        }
    }

    @Test
    @DisplayName("Should return 90-day growth data when days=90")
    public void getStatsWithDays90ShouldReturn90DayWindow() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats with days=90
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .param("days", "90")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        // And: Growth data dates should be within last 90 days
        final LocalDate today = LocalDate.now();
        final LocalDate ninetyDaysAgo = today.minusDays(90);

        for (final GrowthDataPoint dataPoint : stats.getGrowthData()) {
            assertThat(dataPoint.getDate(), greaterThanOrEqualTo(ninetyDaysAgo));
        }
    }

    @Test
    @DisplayName("Should return 400 when days param is invalid value 15")
    public void getStatsWithInvalidDays15ShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats with invalid days=15
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .param("days", "15")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when days param is 0")
    public void getStatsWithDays0ShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats with days=0
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .param("days", "0")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when days param is negative")
    public void getStatsWithNegativeDaysShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats with days=-1
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .param("days", "-1")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when days param is non-numeric")
    public void getStatsWithNonNumericDaysShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats with days=abc
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .param("days", "abc")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    // ==================== New stats fields tests ====================

    @Test
    @DisplayName("Should return channel stats fields in response")
    public void getStatsShouldIncludeChannelStats() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should contain channel stats
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats.getTotalChannels(), is(notNullValue()));
        assertThat(stats.getActiveChannels(), is(notNullValue()));
        assertThat(stats.getPausedChannels(), is(notNullValue()));
        assertThat(stats.getStaleChannels(), is(notNullValue()));
        assertThat(stats.getPendingDeletionChannels(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return non-negative channel counts")
    public void getStatsShouldReturnNonNegativeChannelCounts() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: All channel counts should be non-negative
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats.getTotalChannels(), is(greaterThanOrEqualTo(0L)));
        assertThat(stats.getActiveChannels(), is(greaterThanOrEqualTo(0L)));
        assertThat(stats.getPausedChannels(), is(greaterThanOrEqualTo(0L)));
        assertThat(stats.getStaleChannels(), is(greaterThanOrEqualTo(0L)));
        assertThat(stats.getPendingDeletionChannels(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    @DisplayName("Should return totalEventsInPeriod in response")
    public void getStatsShouldIncludeTotalEventsInPeriod() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should contain totalEventsInPeriod
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        assertThat(stats.getTotalEventsInPeriod(), is(notNullValue()));
        assertThat(stats.getTotalEventsInPeriod(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    @DisplayName("Should return growth data points with event counts")
    public void getStatsShouldReturnGrowthDataWithEventCounts() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting admin stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Growth data points should include event counts
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminStatsResponse stats = fromJson(content, AdminStatsResponse.class);

        for (final GrowthDataPoint dataPoint : stats.getGrowthData()) {
            assertThat(dataPoint.getNewEvents(), is(greaterThanOrEqualTo(0)));
        }
    }

    // ==================== Storage endpoint tests ====================

    @Test
    @DisplayName("Should return storage stats when admin requests")
    public void getStorageStatsSuccess() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting storage stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH + "/storage")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final List<TableSizeEntry> storageStats = fromJson(content, List.class);

        assertThat(storageStats, is(notNullValue()));
        assertThat(storageStats, not(empty()));
    }

    @Test
    @DisplayName("Should return storage stats for all 5 expected tables")
    public void getStorageStatsShouldReturnAllTables() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting storage stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH + "/storage")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should contain 5 table entries
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        @SuppressWarnings("unchecked") final List<Object> storageStats = fromJson(content, List.class);

        assertThat(storageStats, hasSize(5));
    }

    @Test
    @DisplayName("Should reject storage stats request from non-admin user")
    public void getStorageStatsUnauthorizedFails() throws Exception {
        // Given: A regular validated user without admin role
        final User regularUser = aValidatedUser();

        // When: Requesting storage stats without admin authority
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH + "/storage")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject unauthenticated storage stats request")
    public void getStorageStatsWithoutAuthenticationFails() throws Exception {
        // Given: No authentication provided

        // When: Requesting storage stats without authentication
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_PATH + "/storage")
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }
}
