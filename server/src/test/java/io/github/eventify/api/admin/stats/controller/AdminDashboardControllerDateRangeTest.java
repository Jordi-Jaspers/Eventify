package io.github.eventify.api.admin.stats.controller;

import io.github.eventify.api.admin.stats.model.request.AdminStatsRequest;
import io.github.eventify.api.admin.stats.model.response.AdminEventStatsResponse;
import io.github.eventify.api.admin.stats.model.response.AdminEventVolumeResponse;
import io.github.eventify.api.admin.stats.model.response.AdminGrowthResponse;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ADMIN_STATS_EVENTS_PATH;
import static io.github.eventify.api.Paths.ADMIN_STATS_EVENT_VOLUME_PATH;
import static io.github.eventify.api.Paths.ADMIN_STATS_GROWTH_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Admin Dashboard Controller (Date Range)")
public class AdminDashboardControllerDateRangeTest extends IntegrationTest {

    // ==================== POST growth with days mode ====================

    @Test
    @DisplayName("Should return growth data when days mode is used")
    public void getGrowthWithDaysModeSuccess() throws Exception {
        // Given: an authenticated admin user with a days-only request
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest().setDays(30);

        // When: posting to growth endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is successful with 31 data points (30 days + today)
        response.andExpect(status().is(SC_OK));

        final AdminGrowthResponse growth = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdminGrowthResponse.class
        );
        assertThat(growth, is(notNullValue()));
        assertThat(growth.getGrowthData(), hasSize(31));
    }

    // ==================== POST growth with date range mode ====================

    @Test
    @DisplayName("Should return growth data when valid startDate and endDate are provided")
    public void getGrowthWithExplicitDateRangeSuccess() throws Exception {
        // Given: an authenticated admin user with a date range request
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 1, 1))
            .setEndDate(LocalDate.of(2024, 1, 31));

        // When: posting to growth endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is successful with 31 data points (Jan 1 - Jan 31 inclusive)
        response.andExpect(status().is(SC_OK));

        final AdminGrowthResponse growth = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdminGrowthResponse.class
        );
        assertThat(growth, is(notNullValue()));
        assertThat(growth.getGrowthData(), hasSize(31));
    }

    @Test
    @DisplayName("Should return growth data when startDate equals endDate (single day)")
    public void getGrowthWithSameDayRangeSuccess() throws Exception {
        // Given: an authenticated admin user with same-day range
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final LocalDate sameDay = LocalDate.of(2024, 6, 15);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(sameDay)
            .setEndDate(sameDay);

        // When: posting to growth endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is successful with exactly 1 data point
        response.andExpect(status().is(SC_OK));

        final AdminGrowthResponse growth = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdminGrowthResponse.class
        );
        assertThat(growth.getGrowthData(), hasSize(1));
    }

    @Test
    @DisplayName("Should return growth data when startDate is in the far past")
    public void getGrowthWithFarPastStartDateSuccess() throws Exception {
        // Given: an authenticated admin user
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2000, 1, 1))
            .setEndDate(LocalDate.of(2000, 1, 7));

        // When: posting to growth endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is successful
        response.andExpect(status().is(SC_OK));
    }

    // ==================== POST growth — mutual exclusivity errors ====================

    @Test
    @DisplayName("Should return 400 when both days and date range are provided for growth")
    public void getGrowthWithBothModesShouldReturn400() throws Exception {
        // Given: a request with both days and date range set
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setDays(7)
            .setStartDate(LocalDate.of(2024, 1, 1))
            .setEndDate(LocalDate.of(2024, 1, 31));

        // When: posting to growth endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when neither days nor date range are provided for growth")
    public void getGrowthWithNoModeShouldReturn400() throws Exception {
        // Given: an empty request
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest();

        // When: posting to growth endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when only startDate is provided without endDate for growth")
    public void getGrowthWithOnlyStartDateShouldReturn400() throws Exception {
        // Given: request with only startDate
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 1, 1));

        // When: posting to growth endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when only endDate is provided without startDate for growth")
    public void getGrowthWithOnlyEndDateShouldReturn400() throws Exception {
        // Given: request with only endDate
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setEndDate(LocalDate.of(2024, 1, 31));

        // When: posting to growth endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when startDate is after endDate for growth")
    public void getGrowthWithStartDateAfterEndDateShouldReturn400() throws Exception {
        // Given: inverted date range
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 1, 31))
            .setEndDate(LocalDate.of(2024, 1, 1));

        // When: posting to growth endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when endDate is in the future for growth")
    public void getGrowthWithFutureEndDateShouldReturn400() throws Exception {
        // Given: future endDate
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 1, 1))
            .setEndDate(LocalDate.of(2099, 12, 31));

        // When: posting to growth endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_GROWTH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    // ==================== POST event-volume with days mode ====================

    @Test
    @DisplayName("Should return event volume when days mode is used")
    public void getEventVolumeWithDaysModeSuccess() throws Exception {
        // Given: an authenticated admin user with days request
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest().setDays(30);

        // When: posting to event-volume endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is successful with 31 data points
        response.andExpect(status().is(SC_OK));

        final AdminEventVolumeResponse volume = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdminEventVolumeResponse.class
        );
        assertThat(volume.getDailyVolume(), hasSize(31));
    }

    // ==================== POST event-volume with date range mode ====================

    @Test
    @DisplayName("Should return event volume when valid startDate and endDate are provided")
    public void getEventVolumeWithExplicitDateRangeSuccess() throws Exception {
        // Given: an authenticated admin user with a date range request
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 3, 1))
            .setEndDate(LocalDate.of(2024, 3, 7));

        // When: posting to event-volume endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is successful with 7 data points
        response.andExpect(status().is(SC_OK));

        final AdminEventVolumeResponse volume = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdminEventVolumeResponse.class
        );
        assertThat(volume, is(notNullValue()));
        assertThat(volume.getDailyVolume(), hasSize(7));
    }

    // ==================== POST event-volume — mutual exclusivity errors ====================

    @Test
    @DisplayName("Should return 400 when both days and date range provided for event-volume")
    public void getEventVolumeWithBothModesShouldReturn400() throws Exception {
        // Given: request with both modes
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setDays(7)
            .setStartDate(LocalDate.of(2024, 1, 1))
            .setEndDate(LocalDate.of(2024, 1, 31));

        // When: posting to event-volume endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when neither mode provided for event-volume")
    public void getEventVolumeWithNoModeShouldReturn400() throws Exception {
        // Given: empty request
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest();

        // When: posting to event-volume endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when only startDate provided for event-volume")
    public void getEventVolumeWithOnlyStartDateShouldReturn400() throws Exception {
        // Given: request with only startDate
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 1, 1));

        // When: posting to event-volume endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when only endDate provided for event-volume")
    public void getEventVolumeWithOnlyEndDateShouldReturn400() throws Exception {
        // Given: request with only endDate
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setEndDate(LocalDate.of(2024, 1, 31));

        // When: posting to event-volume endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when startDate is after endDate for event-volume")
    public void getEventVolumeWithStartDateAfterEndDateShouldReturn400() throws Exception {
        // Given: inverted date range
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 6, 30))
            .setEndDate(LocalDate.of(2024, 6, 1));

        // When: posting to event-volume endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when endDate is in the future for event-volume")
    public void getEventVolumeWithFutureEndDateShouldReturn400() throws Exception {
        // Given: future endDate
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 1, 1))
            .setEndDate(LocalDate.of(2099, 12, 31));

        // When: posting to event-volume endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENT_VOLUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    // ==================== POST event-stats with days mode ====================

    @Test
    @DisplayName("Should return event stats when days mode is used")
    public void getEventStatsWithDaysModeSuccess() throws Exception {
        // Given: an authenticated admin user with days request
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest().setDays(30);

        // When: posting to event-stats endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is successful
        response.andExpect(status().is(SC_OK));

        final AdminEventStatsResponse stats = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdminEventStatsResponse.class
        );
        assertThat(stats, is(notNullValue()));
    }

    // ==================== POST event-stats with date range mode ====================

    @Test
    @DisplayName("Should return event stats when valid startDate and endDate are provided")
    public void getEventStatsWithExplicitDateRangeSuccess() throws Exception {
        // Given: an authenticated admin user with a date range request
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 2, 1))
            .setEndDate(LocalDate.of(2024, 2, 29));

        // When: posting to event-stats endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is successful
        response.andExpect(status().is(SC_OK));

        final AdminEventStatsResponse stats = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdminEventStatsResponse.class
        );
        assertThat(stats, is(notNullValue()));
    }

    // ==================== POST event-stats — mutual exclusivity errors ====================

    @Test
    @DisplayName("Should return 400 when both days and date range provided for event-stats")
    public void getEventStatsWithBothModesShouldReturn400() throws Exception {
        // Given: request with both modes
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setDays(7)
            .setStartDate(LocalDate.of(2024, 1, 1))
            .setEndDate(LocalDate.of(2024, 1, 31));

        // When: posting to event-stats endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when neither mode provided for event-stats")
    public void getEventStatsWithNoModeShouldReturn400() throws Exception {
        // Given: empty request
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest();

        // When: posting to event-stats endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when only startDate provided for event-stats")
    public void getEventStatsWithOnlyStartDateShouldReturn400() throws Exception {
        // Given: request with only startDate
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 1, 1));

        // When: posting to event-stats endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when only endDate provided for event-stats")
    public void getEventStatsWithOnlyEndDateShouldReturn400() throws Exception {
        // Given: request with only endDate
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setEndDate(LocalDate.of(2024, 1, 31));

        // When: posting to event-stats endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when startDate is after endDate for event-stats")
    public void getEventStatsWithStartDateAfterEndDateShouldReturn400() throws Exception {
        // Given: inverted date range
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 12, 31))
            .setEndDate(LocalDate.of(2024, 1, 1));

        // When: posting to event-stats endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when endDate is in the future for event-stats")
    public void getEventStatsWithFutureEndDateShouldReturn400() throws Exception {
        // Given: future endDate
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);
        final AdminStatsRequest request = new AdminStatsRequest()
            .setStartDate(LocalDate.of(2024, 1, 1))
            .setEndDate(LocalDate.of(2099, 1, 1));

        // When: posting to event-stats endpoint
        final MockHttpServletRequestBuilder req = post(ADMIN_STATS_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(req);

        // Then: response is 400 Bad Request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }
}
