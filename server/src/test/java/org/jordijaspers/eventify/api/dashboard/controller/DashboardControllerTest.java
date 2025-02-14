package org.jordijaspers.eventify.api.dashboard.controller;

import java.util.List;
import java.util.UUID;

import org.hawaiiframework.web.resource.ApiErrorResponseResource;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.type.TypeReference;

import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.jordijaspers.eventify.api.Paths.DASHBOARDS_PATH;
import static org.jordijaspers.eventify.api.Paths.DASHBOARD_PATH;
import static org.jordijaspers.eventify.common.constants.Constants.Security.BEARER;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.fromJson;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.toJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("DashboardController Integration Tests")
public class DashboardControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return unauthorized when requesting dashboards without authentication")
    public void shouldReturnUnauthorizedWhenRequestedWithoutAuthentication() throws Exception {
        // When: requesting all dashboards without authentication
        final MockHttpServletRequestBuilder request = get(DASHBOARDS_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return forbidden when requesting dashboards with incorrect role")
    public void shouldReturnBadRequestWhenRequestedWithIncorrectRole() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.NONE);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: requesting all dashboards with incorrect role
        final MockHttpServletRequestBuilder request = get(DASHBOARDS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return all relevant dashboards when authenticated")
    public void shouldReturnAllRelevantDashboardsWhenAuthenticated() throws Exception {
        // Given: existing teams
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
        final MockHttpServletRequestBuilder request = get(DASHBOARDS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: response should contain all dashboards for team A and global dashboards
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<DashboardResponse> dashboards = fromJson(content, new TypeReference<>() {});

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
    public void shouldCreateDashboardSuccessfully() throws Exception {
        // Given: existing team
        final Team teamA = aValidTeam();

        // And: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);
        addUserToTeam(user, teamA);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: a valid create dashboard request
        final CreateDashboardRequest request = aValidCreateDashboardRequest(teamA);

        // When: creating a dashboard
        final MockHttpServletRequestBuilder createRequest = post(DASHBOARDS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: response should indicate creation
        response.andExpect(status().is(SC_CREATED));

        // And: response should contain created dashboard
        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardResponse dashboard = fromJson(content, DashboardResponse.class);

        assertThat(dashboard.getName(), equalTo(request.getName()));
        assertThat(dashboard.getDescription(), equalTo(request.getDescription()));
    }

    @Test
    @DisplayName("Should fail creating dashboard with incorrect role")
    public void shouldFailCreatingDashboardWithIncorrectRole() throws Exception {
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
        final MockHttpServletRequestBuilder createRequest = post(DASHBOARDS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: response should indicate FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail creating dashboard with invalid request")
    public void shouldFailCreatingDashboardWithInvalidRequest() throws Exception {
        // Given: existing team
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
        final MockHttpServletRequestBuilder createRequest = post(DASHBOARDS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: response should indicate bad request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should update dashboard successfully if part of team and authorized")
    public void shouldUpdateDashboardSuccessfully() throws Exception {
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
        final MockHttpServletRequestBuilder request = put(DASHBOARD_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: dashboard should be updated
        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardResponse updatedDashboard = fromJson(content, DashboardResponse.class);

        assertThat(updatedDashboard.getName(), equalTo(updatedName));
        assertThat(updatedDashboard.getDescription(), equalTo(updatedDescription));
        assertThat(updatedDashboard.isGlobal(), equalTo(true));
    }

    @Test
    @DisplayName("Should fail updating dashboard when not authorized")
    public void shouldFailUpdatingDashboardWhenNotAuthorized() throws Exception {
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
        final MockHttpServletRequestBuilder request = put(DASHBOARD_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail updating dashboard when not part of team")
    public void shouldFailUpdatingDashboardWhenNotPartOfTeam() throws Exception {
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
        final MockHttpServletRequestBuilder request = put(DASHBOARD_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: The reason is in the response
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), equalTo(ApiErrorCode.CANNOT_ACCESS_DASHBOARD.getReason()));
    }

    @Test
    @DisplayName("Should fail updating dashboard with incorrect role")
    public void shouldFailUpdatingDashboardWithIncorrectRole() throws Exception {
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
        final MockHttpServletRequestBuilder request = put(DASHBOARD_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be Forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should be able to update a dashboard of a different team when global")
    public void shouldBeAbleToUpdateDashboardOfDifferentTeamWhenGlobal() throws Exception {
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
        final MockHttpServletRequestBuilder request = put(DASHBOARD_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: dashboard should be updated
        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardResponse updatedDashboard = fromJson(content, DashboardResponse.class);

        assertThat(updatedDashboard.getName(), equalTo(updatedName));
        assertThat(updatedDashboard.getDescription(), equalTo(updatedDescription));
        assertThat(updatedDashboard.isGlobal(), equalTo(true));
    }

    @Test
    @DisplayName("Should delete dashboard successfully when part of the team and authorized")
    public void shouldDeleteDashboardSuccessfully() throws Exception {
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
        final MockHttpServletRequestBuilder request = delete(DASHBOARD_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be successful
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should fail deleting dashboard with incorrect role")
    public void shouldFailDeletingDashboardWithIncorrectRole() throws Exception {
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
        final MockHttpServletRequestBuilder request = delete(DASHBOARD_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail deleting dashboard when not part of team")
    public void shouldFailDeletingDashboardWhenNotPartOfTeam() throws Exception {
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
        final MockHttpServletRequestBuilder request = delete(DASHBOARD_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail deleting dashboard when not authorized")
    public void shouldFailDeletingDashboardWhenNotAuthorized() throws Exception {
        // Given: An existing team
        final Team teamA = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamA);

        // When: deleting the dashboard
        final MockHttpServletRequestBuilder request = delete(DASHBOARD_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail and not delete dashboard when not part of team and global")
    public void shouldFailDeletingDashboardWhenNotPartOfTeamAndGlobal() throws Exception {
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
        final MockHttpServletRequestBuilder request = delete(DASHBOARD_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: The reason is in the response
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), equalTo(ApiErrorCode.USER_NOT_PART_OF_TEAM.getReason()));

        // And: dashboard should still exist
        final Dashboard entity = dashboardService.getDashboardConfiguration(dashboard.getId());
        assertThat(entity, notNullValue());
    }
}
