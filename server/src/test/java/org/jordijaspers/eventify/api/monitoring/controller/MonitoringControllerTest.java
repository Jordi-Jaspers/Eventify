package org.jordijaspers.eventify.api.monitoring.controller;

import io.restassured.module.mockmvc.response.MockMvcResponse;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.jordijaspers.eventify.api.Paths.MONITORING_STREAM_PATH;
import static org.jordijaspers.eventify.api.event.model.Status.CRITICAL;
import static org.jordijaspers.eventify.api.event.model.Status.OK;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

@DisplayName("MonitoringController Integration Tests")
class MonitoringControllerTest extends IntegrationTest {

    private static final Duration DEFAULT_WINDOW = Duration.ofHours(1);

    @Test
    @DisplayName("Should reject unauthenticated user")
    void shouldRejectUnauthenticatedUser() {
        // Given: A dashboard exists
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // When: Requesting dashboard stream without authentication
        final MockMvcResponse response = given()
            .accept(APPLICATION_JSON, TEXT_EVENT_STREAM)
            .queryParam("window", DEFAULT_WINDOW.toString())
            .when()
            .get(MONITORING_STREAM_PATH, dashboard.getId())
            .andReturn();

        // Then: Should return unauthorized
        response.then().statusCode(SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should reject user without READ_DASHBOARDS authority")
    void shouldRejectUserWithoutReadDashboardsAuthority() {
        // Given: A dashboard and user without proper authority exist
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // And: A user without READ_DASHBOARDS authority exists
        final User user = aValidatedUserWithAuthority(Authority.NONE);

        // When: Requesting dashboard stream
        final MockMvcResponse response = given()
            .accept(APPLICATION_JSON, TEXT_EVENT_STREAM)
            .header(AUTHORIZATION, "Bearer " + user.getAccessToken().getValue())
            .queryParam("window", DEFAULT_WINDOW.toString())
            .when()
            .get(MONITORING_STREAM_PATH, dashboard.getId())
            .andReturn();

        // Then: Should return SC_BAD_REQUEST
        response.then().statusCode(SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Should reject user not in dashboard team")
    void shouldRejectUserNotInDashboardTeam() {
        // Given: A dashboard exists
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // And: A user from different team exists
        final User user = aValidatedUser();

        // When: Requesting dashboard stream
        final MockMvcResponse response = given()
            .accept(APPLICATION_JSON, TEXT_EVENT_STREAM)
            .header(AUTHORIZATION, "Bearer " + user.getAccessToken().getValue())
            .queryParam("window", DEFAULT_WINDOW.toString())
            .when()
            .get(MONITORING_STREAM_PATH, dashboard.getId())
            .andReturn();

        // Then: Should return SC_BAD_REQUEST
        response.then().statusCode(SC_BAD_REQUEST);

        // And: Should return correct error code
        response.then().body("apiErrorReason", equalTo(ApiErrorCode.CANNOT_ACCESS_DASHBOARD.getReason()));
    }

    @Test
    @DisplayName("Should allow team member to subscribe to dashboard")
    void shouldAllowTeamMemberToSubscribeToDashboard() throws JsonProcessingException {
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
        final MockMvcResponse response = given()
            .accept(APPLICATION_JSON, TEXT_EVENT_STREAM)
            .header(AUTHORIZATION, "Bearer " + user.getAccessToken().getValue())
            .queryParam("window", DEFAULT_WINDOW.toString())
            .when()
            .get(MONITORING_STREAM_PATH, dashboard.getId())
            .prettyPeek()
            .andReturn();

        // Then: Should return success
        response.then().statusCode(SC_OK);
        response.then().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE);

        // And: The event should be "INITIALIZED"
        final Map<String, String> eventData = parseStream(response.body().asString());
        assertThat(eventData.get("event"), equalTo("INITIALIZED"));

        // And: The data should contain the dashboard subscription
        final DashboardSubscription init = objectMapper.readValue(eventData.get("data"), DashboardSubscription.class);
        assertThat(init.getDashboardId(), equalTo(dashboard.getId()));
        assertThat(init.getWindow(), equalTo(DEFAULT_WINDOW));
        assertThat(init.getUngroupedChecks().size(), equalTo(1));
        assertThat(init.getTimeline().getDurations().size(), greaterThanOrEqualTo(3));
    }


    @Test
    @DisplayName("Should receive updates for subscribed dashboard")
    void shouldReceiveUpdatesForSubscribedDashboard() throws JsonProcessingException {
        // Given: A dashboard exists with team member
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // And: The dashboard has a check
        final Check check = aValidCheck(aValidSource());
        final DashboardConfigurationRequest request = new DashboardConfigurationRequest()
            .setUngroupedCheckIds(Set.of(check.getId()));
        configureDashboard(dashboard.getId(), request);

        // And: Generate some events
        generateEvents(check.getId(), OK, CRITICAL, OK, OK, CRITICAL);

        // And: Add the user to the team
        final User user = aValidatedUser();
        addUserToTeam(user, team);

        // And: User is subscribed to dashboard
        final MockMvcResponse response = subscribeToStream(dashboard.getId(), user);
        response.then().statusCode(SC_OK);

        // When: An event occurs
        final EventRequest event = anEventRequest(check.getId(), OK);
        timelineStreamingService.updateTimelineForCheck(List.of(event), check.getId());

        // Then: Should contain the updated timelines. under the event type "update"
        final Map<String, String> eventData = parseStream(response.body().asString());
        assertThat(eventData.get("event"), equalTo("UPDATE"));

        // And: The data should contain the dashboard subscription
        final DashboardSubscription init = objectMapper.readValue(eventData.get("data"), DashboardSubscription.class);
        assertThat(init.getDashboardId(), equalTo(dashboard.getId()));
        assertThat(init.getWindow(), equalTo(DEFAULT_WINDOW));
        assertThat(init.getUngroupedChecks().size(), equalTo(1));
        assertThat(init.getTimeline().getDurations().size(), greaterThanOrEqualTo(4));
    }

    @Test
    @DisplayName("Should handle subscription with custom window")
    void shouldHandleSubscriptionWithCustomWindow() {
        // Given: A dashboard exists with team member
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);

        // And: Add the user to the team
        final User user = aValidatedUser();
        addUserToTeam(user, team);

        // When: Subscribing with custom window
        final Duration customWindow = Duration.ofMinutes(30);
        final MockMvcResponse response = given()
            .accept(APPLICATION_JSON, TEXT_EVENT_STREAM)
            .header(AUTHORIZATION, "Bearer " + user.getAccessToken().getValue())
            .queryParam("window", customWindow.toString())
            .when()
            .get(MONITORING_STREAM_PATH, dashboard.getId())
            .andReturn();

        // Then: Should return success
        response.then().statusCode(SC_OK);
    }

    @Test
    @DisplayName("Should not subscribe to non-existing dashboard")
    void shouldNotSubscribeToNonExistingDashboard() {
        // Given: A team member exists
        final User user = aValidatedUser();

        // When: Subscribing to non-existing dashboard
        final MockMvcResponse response = subscribeToStream(999L, user);

        // Then: Should return not found
        response.then().statusCode(SC_BAD_REQUEST);

        // And: Should return correct error code
        response.then().body("apiErrorReason", equalTo(ApiErrorCode.DASHBOARD_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should subscribe to global dashboard without being in the team")
    void shouldSubscribeToGlobalDashboardWithoutBeingInTheTeam() {
        // Given: A global dashboard exists
        final Team team = aValidTeam();
        final Dashboard dashboard = aValidDashboard(team);
        dashboard.setGlobal(true);
        dashboardRepository.save(dashboard);

        // And: A team member exists
        final User user = aValidatedUser();

        // When: Subscribing to global dashboard
        final MockMvcResponse response = subscribeToStream(dashboard.getId(), user);

        // Then: Should return success
        response.then().statusCode(SC_OK);
    }

    private MockMvcResponse subscribeToStream(final Long dashboardId, final User user) {
        return given()
            .accept(APPLICATION_JSON, TEXT_EVENT_STREAM)
            .header(AUTHORIZATION, "Bearer " + user.getAccessToken().getValue())
            .queryParam("window", DEFAULT_WINDOW.toString())
            .when()
            .get(MONITORING_STREAM_PATH, dashboardId)
            .andReturn();
    }

    private static Map<String, String> parseStream(final String responseBody) {
        final Map<String, String> eventData = new HashMap<>();
        final String[] eventLines = responseBody.split("\n");
        for (final String line : eventLines) {
            if (line.startsWith("event:")) {
                eventData.put("event", line.substring(6).trim());
            } else if (line.startsWith("data:")) {
                eventData.put("data", line.substring(5).trim());
            }
        }
        return eventData;
    }
}
