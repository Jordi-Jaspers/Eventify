package org.jordijaspers.eventify.api.monitoring.controller;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawaiiframework.web.resource.ApiErrorResponseResource;
import org.hawaiiframework.web.resource.ValidationErrorResponseResource;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.request.DashboardConfigurationRequest;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.monitoring.model.DashboardSubscription;
import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.jordijaspers.eventify.support.util.SseTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;

import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.MONITORING_STREAM_PATH;
import static org.jordijaspers.eventify.api.event.model.Status.CRITICAL;
import static org.jordijaspers.eventify.api.event.model.Status.OK;
import static org.jordijaspers.eventify.api.monitoring.model.validator.WindowValidator.*;
import static org.jordijaspers.eventify.common.constants.Constants.ServerEvents.INITIALIZED;
import static org.jordijaspers.eventify.common.constants.Constants.ServerEvents.UPDATED;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@DisplayName("MonitoringController Integration Tests")
public class MonitoringControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should reject unauthenticated user")
    void shouldRejectUnauthenticatedUser() throws Exception {
        // Given: A dashboard exists
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // When: Requesting dashboard stream without authentication
        final ResultActions response = subscribeToStream(dashboard.getId(), null);

        // Then: Should return Unauthorized
        response.andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("Should reject user without READ_DASHBOARDS authority")
    void shouldRejectUserWithoutReadDashboardsAuthority() throws Exception {
        // Given: A dashboard and user without proper authority exist
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // And: A user without READ_DASHBOARDS authority exists
        final User user = aValidatedUserWithAuthority(Authority.NONE);

        // When: Requesting dashboard stream
        final ResultActions response = subscribeToStream(dashboard.getId(), user.getAccessToken().getValue());

        // Then: Should return Forbidden
        response.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject user not in dashboard team")
    void shouldRejectUserNotInDashboardTeam() throws Exception {
        // Given: A dashboard exists
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // And: A user from different team exists
        final User user = aValidatedUser();

        // When: Requesting dashboard stream
        final ResultActions response = subscribeToStream(dashboard.getId(), user.getAccessToken().getValue());

        // Then: Should return SC_BAD_REQUEST
        response.andExpect(status().isBadRequest());

        // And: Should return correct error code
        final String object = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource errorResponse = readValue(object, ApiErrorResponseResource.class);
        assertThat(errorResponse.getApiErrorReason(), equalTo(ApiErrorCode.CANNOT_ACCESS_DASHBOARD.getReason()));
    }

    @Test
    @DisplayName("Should allow team member to subscribe to dashboard")
    void shouldAllowTeamMemberToSubscribeToDashboard() throws Exception {
        // Given: A dashboard exists
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // And: The dashboard has a check
        final Check check = aValidCheck(aValidSource());
        final DashboardConfigurationRequest request = new DashboardConfigurationRequest()
            .setUngroupedCheckIds(Set.of(check.getId()));
        configureDashboard(dashboard.getId(), request);

        // And: Generate some events
        generateEvents(check.getId(), OK, CRITICAL, OK, OK, CRITICAL);

        // And: A team member exists
        final User user = aValidatedUser();
        addUserToTeam(user, team);

        // When: Requesting dashboard stream
        final ResultActions response = subscribeToStream(dashboard.getId(), user.getAccessToken().getValue());

        // Then: The stream should be started.
        response.andExpect(status().isOk());
        final MvcResult mvcResult = response.andExpect(request().asyncStarted()).andReturn();

        // And: Collect the events
        final List<Map<String, String>> events = SseTestUtils.collectEvents(mvcResult, INITIALIZED, Duration.ofSeconds(5));

        // And: Get the initialization event
        final Map<String, String> initEvent = events.stream()
            .filter(e -> INITIALIZED.equals(e.get("event")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No initialization event found"));

        // And: Verify subscription details
        final DashboardSubscription init = readValue(initEvent.get("data"), DashboardSubscription.class);
        assertThat(init.getDashboardId(), equalTo(dashboard.getId()));
        assertThat(init.getWindow().toString(), equalTo(DEFAULT_WINDOW));
        assertThat(init.getUngroupedChecks().size(), equalTo(1));
        assertThat(init.getUngroupedChecks().getFirst().getTimeline().getDurations().size(), greaterThanOrEqualTo(3));
        assertThat(init.getTimeline().getDurations().size(), greaterThanOrEqualTo(3));
    }

    @Test
    @DisplayName("Should receive updates for subscribed dashboard")
    public void shouldReceiveUpdatesForSubscribedDashboard() throws Exception {
        // Given: A dashboard exists with team member
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // And: Generate some events
        final Check check = aValidCheck(aValidSource());
        generateEvents(check.getId(), OK, CRITICAL, OK, OK, CRITICAL);

        // And: The dashboard has a check
        final DashboardConfigurationRequest request = new DashboardConfigurationRequest()
            .setUngroupedCheckIds(Set.of(check.getId()));
        configureDashboard(dashboard.getId(), request);

        // And: Add the user to the team
        final User user = aValidatedUser();
        addUserToTeam(user, team);

        // When: Subscribe to the SSE stream
        final ResultActions response = subscribeToStream(dashboard.getId(), user.getAccessToken().getValue());

        // And: The stream should be started.
        response.andExpect(status().isOk());
        final MvcResult mvcResult = response.andExpect(request().asyncStarted()).andReturn();

        // And: Trigger an event
        final EventRequest event = anEventRequest(check.getId(), OK);
        timelineStreamingService.updateTimelineForCheck(List.of(event), check.getId());

        // Then: Collect and verify update events
        final List<Map<String, String>> events = SseTestUtils.collectEvents(mvcResult, UPDATED, Duration.ofSeconds(5));
        final Map<String, String> updateEvent = events.stream()
            .filter(e -> UPDATED.equals(e.get("event")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No update event found"));

        final DashboardSubscription update = readValue(updateEvent.get("data"), DashboardSubscription.class);
        assertThat(update.getDashboardId(), equalTo(dashboard.getId()));
        assertThat(update.getWindow().toString(), equalTo(DEFAULT_WINDOW));
        assertThat(update.getUngroupedChecks().size(), equalTo(1));
        assertThat(update.getTimeline().getDurations().size(), greaterThanOrEqualTo(4));
    }

    @Test
    @DisplayName("Should not accept subscription with custom window not in hours")
    void shouldNotAcceptSubscriptionWithCustomWindowNotInHours() throws Exception {
        // Given: A dashboard exists with team member
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // And: Add the user to the team
        final User user = aValidatedUser();
        addUserToTeam(user, team);

        // When: Subscribing with custom window
        final Duration customWindow = Duration.ofMinutes(30);
        final ResultActions response = subscribeToStream(dashboard.getId(), user.getAccessToken().getValue(), customWindow);

        // Then: Should return bad request
        response.andExpect(status().isBadRequest());

        // And: Should return correct error code
        final String object = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource errorResponse = readValue(object, ValidationErrorResponseResource.class);
        assertThat(errorResponse, notNullValue());
        errorResponse.getErrors()
            .stream()
            .filter(error -> WINDOW.equals(error.getField()))
            .findFirst()
            .ifPresentOrElse(
                error -> assertThat(error.getCode(), equalTo(WINDOW_MUST_BE_IN_HOURS)),
                () -> assertThat("Window field not found", is(false))
            );
    }

    @Test
    @DisplayName("Should handle subscription with custom window")
    void shouldHandleSubscriptionWithCustomWindow() throws Exception {
        // Given: A dashboard exists with team member
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // And: Add the user to the team
        final User user = aValidatedUser();
        addUserToTeam(user, team);

        // When: Subscribing with custom window
        final Duration customWindow = Duration.ofHours(2);
        final ResultActions response = subscribeToStream(dashboard.getId(), user.getAccessToken().getValue(), customWindow);

        // Then: The stream should be started
        response.andExpect(status().isOk());
        final MvcResult mvcResult = response.andExpect(request().asyncStarted()).andReturn();

        // And: Collect the events
        final List<Map<String, String>> events = SseTestUtils.collectEvents(mvcResult, INITIALIZED, Duration.ofSeconds(5));

        // And: Get the initialization event
        final Map<String, String> initEvent = events.stream()
            .filter(e -> INITIALIZED.equals(e.get("event")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No initialization event found"));

        // And: Verify subscription details
        final DashboardSubscription init = readValue(initEvent.get("data"), DashboardSubscription.class);
        assertThat(init.getDashboardId(), equalTo(dashboard.getId()));
        assertThat(init.getWindow(), equalTo(customWindow));
    }

    @Test
    @DisplayName("Should not subscribe to non-existing dashboard")
    void shouldNotSubscribeToNonExistingDashboard() throws Exception {
        // Given: A team member exists
        final User user = aValidatedUser();

        // When: Subscribing to non-existing dashboard
        final ResultActions response = subscribeToStream(999L, user.getAccessToken().getValue());

        // Then: Should return a Bad Request
        response.andExpect(status().isBadRequest());

        // And: Should return correct error code
        final String object = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource errorResponse = readValue(object, ApiErrorResponseResource.class);
        assertThat(errorResponse.getApiErrorReason(), equalTo(ApiErrorCode.DASHBOARD_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should subscribe to global dashboard without being in the team")
    void shouldSubscribeToGlobalDashboardWithoutBeingInTheTeam() throws Exception {
        // Given: A global dashboard exists
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);
        dashboard.setGlobal(true);
        dashboardRepository.save(dashboard);

        // And: A team member exists
        final User user = aValidatedUser();

        // When: Subscribing to global dashboard
        final ResultActions response = subscribeToStream(dashboard.getId(), user.getAccessToken().getValue(), null);

        // And: The response should be OK
        response.andExpect(status().isOk());

        // And: The connection should be async and alive
        final MvcResult mvcResult = response.andExpect(request().asyncStarted()).andReturn();

        // Then: Collect the events
        final List<Map<String, String>> events = SseTestUtils.collectEvents(mvcResult, INITIALIZED, Duration.ofSeconds(5));

        // And: Get the initialization event
        final Map<String, String> initEvent = events.stream()
            .filter(e -> INITIALIZED.equals(e.get("event")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No initialization event found"));

        // And: Verify subscription details
        final DashboardSubscription init = readValue(initEvent.get("data"), DashboardSubscription.class);
        assertThat(init.getDashboardId(), equalTo(dashboard.getId()));
        assertThat(init.getWindow().toString(), equalTo(DEFAULT_WINDOW));
    }

    private ResultActions subscribeToStream(final Long dashboardId, final String token) {
        return subscribeToStream(dashboardId, token, null);
    }

    private ResultActions subscribeToStream(final Long dashboardId, final String token, final Duration window) {
        try {
            final MockHttpServletRequestBuilder request = get(MONITORING_STREAM_PATH, dashboardId)
                .contentType(APPLICATION_JSON)
                .accept(TEXT_EVENT_STREAM, APPLICATION_JSON);

            if (nonNull(window)) {
                request.queryParam(WINDOW, window.toString());
            }

            if (nonNull(token)) {
                request.header(AUTHORIZATION, "Bearer " + token);
            }

            return mockMvc.perform(request);
        } catch (final Exception exception) {
            throw new AssertionError("There was an error subscribing to the stream", exception);
        }
    }

    private <T> T readValue(final String object, final Class<T> clazz) {
        try {
            return objectMapper.readValue(object, clazz);
        } catch (final JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }
}
