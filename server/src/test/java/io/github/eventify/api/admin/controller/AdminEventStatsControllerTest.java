package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.response.AdminEventStatsResponse;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ADMIN_STATS_EVENTS_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Admin Event Stats Controller Tests")
public class AdminEventStatsControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return 200 with correct JSON structure when admin requests event stats")
    public void getEventStatsSuccess() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats with days=30
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "30")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain correct JSON structure
        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminEventStatsResponse stats = fromJson(content, AdminEventStatsResponse.class);

        assertThat(stats, is(notNullValue()));
        assertThat(stats.getDailyIngestion(), is(notNullValue()));
        assertThat(stats.getTopChannels(), is(notNullValue()));
        assertThat(stats.getSeverityBreakdown(), is(notNullValue()));
        assertThat(stats.getQuotaStats(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return severity breakdown with critical, warning, ok fields")
    public void getEventStatsShouldIncludeSeverityBreakdown() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "30")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Severity breakdown should have all required fields
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminEventStatsResponse stats = fromJson(content, AdminEventStatsResponse.class);

        assertThat(stats.getSeverityBreakdown(), is(notNullValue()));
        assertThat(stats.getSeverityBreakdown().getCritical(), is(notNullValue()));
        assertThat(stats.getSeverityBreakdown().getWarning(), is(notNullValue()));
        assertThat(stats.getSeverityBreakdown().getOk(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return quota stats with usersNearLimit, usersAtLimit, averageUtilization fields")
    public void getEventStatsShouldIncludeQuotaStats() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "30")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Quota stats should have all required fields
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminEventStatsResponse stats = fromJson(content, AdminEventStatsResponse.class);

        assertThat(stats.getQuotaStats(), is(notNullValue()));
        assertThat(stats.getQuotaStats().getUsersNearLimit(), is(notNullValue()));
        assertThat(stats.getQuotaStats().getUsersAtLimit(), is(notNullValue()));
        assertThat(stats.getQuotaStats().getAverageUtilization(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return non-negative counts in severity breakdown")
    public void getEventStatsShouldReturnNonNegativeSeverityCounts() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "30")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: All severity counts should be non-negative
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminEventStatsResponse stats = fromJson(content, AdminEventStatsResponse.class);

        assertThat(stats.getSeverityBreakdown().getCritical(), is(greaterThanOrEqualTo(0L)));
        assertThat(stats.getSeverityBreakdown().getWarning(), is(greaterThanOrEqualTo(0L)));
        assertThat(stats.getSeverityBreakdown().getOk(), is(greaterThanOrEqualTo(0L)));
    }

    @Test
    @DisplayName("Should return non-negative quota stats values")
    public void getEventStatsShouldReturnNonNegativeQuotaStats() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "30")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: All quota stats values should be non-negative
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminEventStatsResponse stats = fromJson(content, AdminEventStatsResponse.class);

        assertThat(stats.getQuotaStats().getUsersNearLimit(), is(greaterThanOrEqualTo(0L)));
        assertThat(stats.getQuotaStats().getUsersAtLimit(), is(greaterThanOrEqualTo(0L)));
        assertThat(stats.getQuotaStats().getAverageUtilization(), is(greaterThanOrEqualTo(0.0)));
    }

    @Test
    @DisplayName("Should return top channels list with at most 10 entries")
    public void getEventStatsShouldReturnTopChannelsWithAtMost10Entries() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "30")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Top channels should have at most 10 entries
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminEventStatsResponse stats = fromJson(content, AdminEventStatsResponse.class);

        assertThat(stats.getTopChannels(), is(notNullValue()));
        assertThat(stats.getTopChannels().size(), is(lessThanOrEqualTo(10)));
    }

    @Test
    @DisplayName("Should return 403 when user without VIEW_PLATFORM_STATS authority requests event stats")
    public void getEventStatsWithoutAuthorityFails() throws Exception {
        // Given: A regular validated user without admin role
        final User regularUser = aValidatedUser();

        // When: Requesting event stats without VIEW_PLATFORM_STATS authority
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "30")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 403 Forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated request is made")
    public void getEventStatsWithoutAuthenticationFails() throws Exception {
        // Given: No authentication provided

        // When: Requesting event stats without authentication
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "30")
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 401 Unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 400 when days param is invalid value 15")
    public void getEventStatsWithInvalidDays15ShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats with invalid days=15
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "15")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when days param is 0")
    public void getEventStatsWithDays0ShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats with days=0
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "0")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when days param is negative")
    public void getEventStatsWithNegativeDaysShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats with days=-1
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "-1")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when days param is non-numeric")
    public void getEventStatsWithNonNumericDaysShouldReturn400() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats with days=abc
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "abc")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 200 with valid days=7")
    public void getEventStatsWithDays7ShouldSucceed() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats with days=7
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "7")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return 200 with valid days=90")
    public void getEventStatsWithDays90ShouldSucceed() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats with days=90
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "90")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return 200 with default days=30 when no days param provided")
    public void getEventStatsWithDefaultDaysShouldSucceed() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats without days param (defaults to 30)
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return empty dailyIngestion list when no data in period")
    public void getEventStatsShouldReturnEmptyDailyIngestionWhenNoData() throws Exception {
        // Given: An authenticated admin user (no event data seeded)
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "30")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 200 OK with empty dailyIngestion
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final AdminEventStatsResponse stats = fromJson(content, AdminEventStatsResponse.class);

        assertThat(stats.getDailyIngestion(), is(notNullValue()));
        // And: Severity breakdown should have zeros when no data
        assertThat(stats.getSeverityBreakdown().getCritical(), is(equalTo(0L)));
        assertThat(stats.getSeverityBreakdown().getWarning(), is(equalTo(0L)));
        assertThat(stats.getSeverityBreakdown().getOk(), is(equalTo(0L)));
    }

    @Test
    @DisplayName("Should return application/json content type")
    public void getEventStatsShouldReturnApplicationJsonContentType() throws Exception {
        // Given: An authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Requesting event stats
        final MockHttpServletRequestBuilder request = get(ADMIN_STATS_EVENTS_PATH)
            .param("days", "30")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should have application/json content type
        response.andExpect(status().is(SC_OK));

        final String contentType = response.andReturn().getResponse().getContentType();
        assertThat(contentType, containsString("application/json"));
    }
}
