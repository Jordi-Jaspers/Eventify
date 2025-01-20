package org.jordijaspers.eventify.api.dashboard.controller;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.response.MockMvcResponse;

import java.util.List;
import java.util.UUID;

import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.request.CreateDashboardRequest;
import org.jordijaspers.eventify.api.dashboard.model.request.UpdateDashboardDetailsRequest;
import org.jordijaspers.eventify.api.dashboard.model.response.DashboardResponse;
import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.jordijaspers.eventify.api.Paths.DASHBOARDS_PATH;
import static org.jordijaspers.eventify.api.Paths.DASHBOARD_PATH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@DisplayName("DashboardController Integration Tests")
public class DashboardControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return unauthorized when requesting dashboards without authentication")
    public void shouldReturnUnauthorizedWhenRequestedWithoutAuthentication() {
        // When: requesting all dashboards without authentication
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(DASHBOARDS_PATH)
            .andReturn();

        // Then: response should be unauthorized
        response.then().statusCode(SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should return forbidden when requesting dashboards with incorrect role")
    public void shouldReturnBadRequestWhenRequestedWithIncorrectRole() {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.NONE);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: requesting all dashboards with incorrect role
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(DASHBOARDS_PATH)
            .andReturn();

        // Then: response should be bad request
        response.then().statusCode(SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Should return all relevant dashboards when authenticated")
    public void shouldReturnAllRelevantDashboardsWhenAuthenticated() {
        // Given: exising teams
        final Team teamA = aValidTeam();
        final Team teamB = aValidTeam();

        // And: Some dashboards
        aValidDashboard(teamA, false);
        aValidDashboard(teamA, false);
        aValidDashboard(teamB, false);
        aValidDashboard(teamB, true);

        // And: authenticated user part of team A
        final User user = aValidatedUser();
        addUserToTeam(user, teamA);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: requesting all dashboards
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(DASHBOARDS_PATH)
            .andReturn();

        // Then: response should be successful
        response.then().statusCode(SC_OK);

        // And: response should contain all dashboards for team A and global dashboards
        final List<DashboardResponse> dashboards = response.body().as(new TypeRef<>() {});
        assertThat(dashboards.size(), equalTo(3));
        dashboards.forEach(dashboard -> {
            if (dashboard.isGlobal()) {
                assertThat(dashboard.getTeam().getId(), equalTo(teamB.getId()));
            } else {
                assertThat(dashboard.getTeam().getId(), equalTo(teamA.getId()));
            }
        });
    }

    @Test
    @DisplayName("Should create dashboard successfully")
    public void shouldCreateDashboardSuccessfully() {
        // Given: exising team
        final Team teamA = aValidTeam();

        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);
        addUserToTeam(user, teamA);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: a valid create dashboard request
        final CreateDashboardRequest request = aValidCreateDashboardRequest(teamA);

        // When: creating a dashboard
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(DASHBOARDS_PATH)
            .andReturn();

        // Then: response should indicate creation
        response.then().statusCode(SC_CREATED);

        // And: response should contain created dashboard
        final DashboardResponse dashboard = response.body().as(DashboardResponse.class);
        assertThat(dashboard.getName(), equalTo(request.getName()));
        assertThat(dashboard.getDescription(), equalTo(request.getDescription()));
    }

    @Test
    @DisplayName("Should fail creating dashboard with incorrect role")
    public void shouldFailCreatingDashboardWithIncorrectRole() {
        // Given: An existing team
        final Team teamA = aValidTeam();

        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.NONE);
        addUserToTeam(user, teamA);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: a valid create dashboard request
        final CreateDashboardRequest request = aValidCreateDashboardRequest(teamA);

        // When: creating a dashboard
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(DASHBOARDS_PATH)
            .andReturn();

        // Then: response should indicate FORBIDDEN
        response.then().statusCode(SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Should fail creating dashboard with invalid request")
    public void shouldFailCreatingDashboardWithInvalidRequest() {
        // Given: exising team
        final Team teamA = aValidTeam();

        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);
        addUserToTeam(user, teamA);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: an invalid create dashboard request (empty name)
        final CreateDashboardRequest request = aValidCreateDashboardRequest(teamA)
            .setName("");

        // When: creating a dashboard
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(DASHBOARDS_PATH)
            .andReturn();

        // Then: response should indicate bad request
        response.then().statusCode(SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Should update dashboard successfully if part of team and authorized")
    public void shouldUpdateDashboardSuccessfully() {
        // Given: An existing team
        final Team teamA = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamA);

        // And: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);
        addUserToTeam(user, teamA);

        // And: a user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: update request for the name and the global flag
        final String updatedDescription = "Updated Description";
        final String updatedName = UUID.randomUUID() + " - " + DASHBOARD_NAME;
        final UpdateDashboardDetailsRequest updateRequest = new UpdateDashboardDetailsRequest()
            .setName(updatedName)
            .setDescription(updatedDescription)
            .setGlobal(true);

        // When: updating the dashboard
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .body(updateRequest)
            .when()
            .put(DASHBOARD_PATH, dashboard.getId())
            .andReturn();

        // Then: response should be successful
        response.then().statusCode(SC_OK);

        // And: dashboard should be updated
        final DashboardResponse updatedDashboard = response.body().as(DashboardResponse.class);
        assertThat(updatedDashboard.getName(), equalTo(updatedName));
        assertThat(updatedDashboard.getDescription(), equalTo(updatedDescription));
        assertThat(updatedDashboard.isGlobal(), equalTo(true));
    }

    @Test
    @DisplayName("Should fail updating dashboard when not authorized")
    public void shouldFailUpdatingDashboardWhenNotAuthorized() {
        // Given: An existing team
        final Team teamA = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamA);

        // And: update request for the name and the global flag
        final String updatedName = "Updated Dashboard";
        final UpdateDashboardDetailsRequest updateRequest = new UpdateDashboardDetailsRequest()
            .setName(updatedName)
            .setDescription(dashboard.getDescription())
            .setGlobal(true);

        // When: updating the dashboard
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(updateRequest)
            .when()
            .put(DASHBOARD_PATH, dashboard.getId())
            .andReturn();

        // Then: response should be UNAUTHORIZED
        response.then().statusCode(SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should fail updating dashboard when not part of team")
    public void shouldFailUpdatingDashboardWhenNotPartOfTeam() {
        // Given: An existing team
        final Team teamA = aValidTeam();
        final Team teamB = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamB);

        // And: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);
        addUserToTeam(user, teamA);

        // And: a user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: update request for the name and the global flag
        final String updatedName = "Updated Dashboard";
        final UpdateDashboardDetailsRequest updateRequest = new UpdateDashboardDetailsRequest()
            .setName(updatedName)
            .setDescription(dashboard.getDescription())
            .setGlobal(true);

        // When: updating the dashboard
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .body(updateRequest)
            .when()
            .put(DASHBOARD_PATH, dashboard.getId())
            .andReturn();

        // Then: response should be bad request
        response.then().statusCode(SC_BAD_REQUEST);

        // And: The reason is in the response
        response.then().body("apiErrorReason", equalTo(ApiErrorCode.CANNOT_ACCESS_DASHBOARD.getReason()));
    }

    @Test
    @DisplayName("Should fail updating dashboard with incorrect role")
    public void shouldFailUpdatingDashboardWithIncorrectRole() {
        // Given: An existing team
        final Team teamA = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamA);

        // And: authenticated user
        final User user = aValidatedUser();
        addUserToTeam(user, teamA);

        // And: a user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: update request for the name and the global flag
        final String updatedName = "Updated Dashboard";
        final UpdateDashboardDetailsRequest updateRequest = new UpdateDashboardDetailsRequest()
            .setName(updatedName)
            .setDescription(dashboard.getDescription())
            .setGlobal(true);

        // When: updating the dashboard
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .body(updateRequest)
            .when()
            .put(DASHBOARD_PATH, dashboard.getId())
            .andReturn();

        // Then: response should be Forbidden
        response.then().statusCode(SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Should be able to update a dashboard of a different team when global")
    public void shouldBeAbleToUpdateDashboardOfDifferentTeamWhenGlobal() {
        // Given: An existing team
        final Team teamA = aValidTeam();
        final Team teamB = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamB, true);

        // And: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);
        addUserToTeam(user, teamA);

        // And: a user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: update request for the name and the global flag
        final String updatedDescription = "Updated Description";
        final String updatedName = UUID.randomUUID() + " - " + DASHBOARD_NAME;
        final UpdateDashboardDetailsRequest updateRequest = new UpdateDashboardDetailsRequest()
            .setName(updatedName)
            .setDescription(updatedDescription)
            .setGlobal(true);

        // When: updating the dashboard
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .body(updateRequest)
            .when()
            .put(DASHBOARD_PATH, dashboard.getId())
            .andReturn();

        // Then: response should be successful
        response.then().statusCode(SC_OK);

        // And: dashboard should be updated
        final DashboardResponse updatedDashboard = response.body().as(DashboardResponse.class);
        assertThat(updatedDashboard.getName(), equalTo(updatedName));
        assertThat(updatedDashboard.getDescription(), equalTo(updatedDescription));
        assertThat(updatedDashboard.isGlobal(), equalTo(true));
    }

    @Test
    @DisplayName("Should delete dashboard successfully when part of the team and authorized")
    public void shouldDeleteDashboardSuccessfully() {
        // Given: An existing team
        final Team teamA = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamA);

        // And: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);
        addUserToTeam(user, teamA);

        // And: a user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: deleting the dashboard
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .delete(DASHBOARD_PATH, dashboard.getId())
            .andReturn();

        // Then: response should be successful
        response.then().statusCode(SC_NO_CONTENT);
    }

    @Test
    @DisplayName("Should fail deleting dashboard with incorrect role")
    public void shouldFailDeletingDashboardWithIncorrectRole() {
        // Given: An existing team
        final Team teamA = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamA);

        // And: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.NONE);
        addUserToTeam(user, teamA);

        // And: a user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: deleting the dashboard
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .delete(DASHBOARD_PATH, dashboard.getId())
            .andReturn();

        // Then: response should be forbidden
        response.then().statusCode(SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Should fail deleting dashboard when not part of team")
    public void shouldFailDeletingDashboardWhenNotPartOfTeam() {
        // Given: An existing team
        final Team teamA = aValidTeam();
        final Team teamB = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamB);

        // And: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);
        addUserToTeam(user, teamA);

        // And: a user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: deleting the dashboard
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .delete(DASHBOARD_PATH, dashboard.getId())
            .andReturn();

        // Then: response should be bad request
        response.then().statusCode(SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("Should fail deleting dashboard when not authorized")
    public void shouldFailDeletingDashboardWhenNotAuthorized() {
        // Given: An existing team
        final Team teamA = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamA);

        // When: deleting the dashboard
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .delete(DASHBOARD_PATH, dashboard.getId())
            .andReturn();

        // Then: response should be unauthorized
        response.then().statusCode(SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should fail and not delete dashboard when not part of team and global")
    public void shouldFailDeletingDashboardWhenNotPartOfTeamAndGlobal() {
        // Given: An existing team
        final Team teamA = aValidTeam();
        final Team teamB = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamB, true);

        // And: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);
        addUserToTeam(user, teamA);

        // And: a user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: deleting the dashboard
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .delete(DASHBOARD_PATH, dashboard.getId())
            .andReturn();

        // Then: response should be bad request
        response.then().statusCode(SC_BAD_REQUEST);

        // And: The reason is in the response
        response.then().body("apiErrorReason", equalTo(ApiErrorCode.USER_NOT_PART_OF_TEAM.getReason()));

        // And: dashboard should still exist
        final Dashboard entity = dashboardService.getDashboardConfiguration(dashboard.getId());
        assertThat(entity, notNullValue());
    }
}
