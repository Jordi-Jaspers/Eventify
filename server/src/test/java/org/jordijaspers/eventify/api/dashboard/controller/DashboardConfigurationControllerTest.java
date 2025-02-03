package org.jordijaspers.eventify.api.dashboard.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.hawaiiframework.web.resource.ApiErrorResponseResource;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.request.DashboardConfigurationRequest;
import org.jordijaspers.eventify.api.dashboard.model.request.DashboardGroupRequest;
import org.jordijaspers.eventify.api.dashboard.model.response.DashboardResponse;
import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.DASHBOARD_CONFIGURATION_PATH;
import static org.jordijaspers.eventify.common.constants.Constants.Security.BEARER;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.fromJson;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.toJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("DashboardConfigurationController Integration Tests")
public class DashboardConfigurationControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return unauthorized when requesting dashboard configuration without authentication")
    public void shouldReturnUnauthorizedWhenRequestedWithoutAuthentication() throws Exception {
        // Given: An existing team
        final Team team = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(team);

        // And: requesting dashboard configuration without authentication
        final MockHttpServletRequestBuilder request = get(DASHBOARD_CONFIGURATION_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON);

        // When: requesting dashboard configuration
        final ResultActions response = mockMvc.perform(request);

        // Then: response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return forbidden when requesting dashboard configuration with incorrect role")
    public void shouldReturnForbiddenWhenRequestedWithIncorrectRole() throws Exception {
        // Given: An existing team
        final Team team = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(team);

        // And: authenticated user with incorrect role
        final User user = aValidatedUserWithAuthority(Authority.NONE);
        addUserToTeam(user, team);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: requesting dashboard configuration
        final MockHttpServletRequestBuilder request = get(DASHBOARD_CONFIGURATION_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: requesting dashboard configuration
        final ResultActions response = mockMvc.perform(request);

        // Then: response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return dashboard configuration when authenticated and part of team")
    public void shouldReturnDashboardConfigurationWhenAuthenticatedAndPartOfTeam() throws Exception {
        // Given: An existing team
        final Team team = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(team);

        // And: authenticated user
        final User user = aValidatedUser();
        addUserToTeam(user, team);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: requesting dashboard configuration
        final MockHttpServletRequestBuilder request = get(DASHBOARD_CONFIGURATION_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: requesting dashboard configuration
        final ResultActions response = mockMvc.perform(request);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: response should contain dashboard configuration
        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardResponse dashboardResponse = fromJson(content, DashboardResponse.class);

        assertThat(dashboardResponse.getId(), equalTo(dashboard.getId()));
        assertThat(dashboardResponse.getName(), equalTo(dashboard.getName()));
    }

    @Test
    @DisplayName("Should return bad request when requesting non-existent dashboard configuration")
    public void shouldReturnBadRequestWhenRequestingNonExistentDashboard() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: requesting non-existent dashboard configuration
        final MockHttpServletRequestBuilder request = get(DASHBOARD_CONFIGURATION_PATH, 999L)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: requesting dashboard configuration
        final ResultActions response = mockMvc.perform(request);

        // Then: response should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: The reason is in the response
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), equalTo(ApiErrorCode.DASHBOARD_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should return dashboard configuration when global and not part of team")
    public void shouldReturnDashboardConfigurationWhenGlobalAndNotPartOfTeam() throws Exception {
        // Given: existing teams
        final Team teamA = aValidTeam();
        final Team teamB = aValidTeam();

        // And: an existing global dashboard
        final Dashboard dashboard = aValidDashboard(teamB, true);

        // And: authenticated user part of different team
        final User user = aValidatedUser();
        addUserToTeam(user, teamA);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: requesting dashboard configuration
        final MockHttpServletRequestBuilder request = get(DASHBOARD_CONFIGURATION_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: requesting dashboard configuration
        final ResultActions response = mockMvc.perform(request);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: response should contain dashboard configuration
        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardResponse dashboardResponse = fromJson(content, DashboardResponse.class);

        assertThat(dashboardResponse.getId(), equalTo(dashboard.getId()));
        assertThat(dashboardResponse.isGlobal(), equalTo(true));
    }

    @Test
    @DisplayName("Should configure dashboard successfully when dashboard is global but not part of team")
    public void shouldConfigureDashboardSuccessfullyWhenGlobalAndNotPartOfTeam() throws Exception {
        // Given: An existing team
        final Team team = aValidTeam();

        // And: an existing global dashboard
        final Dashboard dashboard = aValidDashboard(team, true);

        // And: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: a valid configuration request
        final DashboardConfigurationRequest request = new DashboardConfigurationRequest();

        // And: configuring the dashboard
        final MockHttpServletRequestBuilder updateRequest = put(DASHBOARD_CONFIGURATION_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        // When: configuring the dashboard
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: response should contain updated dashboard configuration
        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardResponse dashboardResponse = fromJson(content, DashboardResponse.class);
        assertThat(dashboardResponse.getId(), equalTo(dashboard.getId()));
    }

    @Test
    @DisplayName("Should configure dashboard successfully when authenticated and part of team")
    public void shouldConfigureDashboardSuccessfullyWhenAuthenticated() throws Exception {
        // Given: An existing team
        final Team team = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(team);

        // And: some checks to configure
        final Source source = aValidSource();
        final List<Check> ungrouped = generateChecks(source, 5);
        final List<Check> grouped = generateChecks(source, 3);

        // And: authenticated user with correct role
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);
        addUserToTeam(user, team);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: a valid configuration request
        final DashboardGroupRequest groupRequest = new DashboardGroupRequest();
        groupRequest.setCheckIds(grouped.stream().map(Check::getId).toList());
        groupRequest.setName("Group 1");

        final DashboardConfigurationRequest request = new DashboardConfigurationRequest();
        request.setUngroupedCheckIds(ungrouped.stream().map(Check::getId).collect(Collectors.toSet()));
        request.setGroups(List.of(groupRequest));

        // And: configuring the dashboard
        final MockHttpServletRequestBuilder updateRequest = put(DASHBOARD_CONFIGURATION_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        // When: configuring the dashboard
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: response should contain updated dashboard configuration
        final String content = response.andReturn().getResponse().getContentAsString();
        final DashboardResponse dashboardResponse = fromJson(content, DashboardResponse.class);

        assertThat(dashboardResponse.getId(), equalTo(dashboard.getId()));
        assertThat(dashboardResponse.getConfiguration().getUngroupedChecks().size(), equalTo(ungrouped.size()));
        assertThat(dashboardResponse.getConfiguration().getGroups().size(), equalTo(1));
        assertThat(dashboardResponse.getConfiguration().getGroups().getFirst().getName(), equalTo(groupRequest.getName()));
        assertThat(dashboardResponse.getConfiguration().getGroups().getFirst().getChecks().size(), equalTo(grouped.size()));
        assertThat(dashboardResponse.getLastUpdated(), not(equalTo(dashboard.getLastUpdated())));
    }

    @Test
    @DisplayName("Should fail configuring dashboard with incorrect role")
    public void shouldFailConfiguringDashboardWithIncorrectRole() throws Exception {
        // Given: An existing team
        final Team team = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(team);

        // And: authenticated user with incorrect role
        final User user = aValidatedUserWithAuthority(Authority.NONE);
        addUserToTeam(user, team);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: a valid configuration request
        final DashboardConfigurationRequest request = new DashboardConfigurationRequest();

        // And: configuring the dashboard
        final MockHttpServletRequestBuilder updateRequest = put(DASHBOARD_CONFIGURATION_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        // When: configuring the dashboard
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail configuring dashboard and not authenticated")
    public void shouldFailConfiguringDashboardAndNotAuthenticated() throws Exception {
        // Given: An existing team
        final Team team = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(team);

        // And: a valid configuration request
        final DashboardConfigurationRequest request = new DashboardConfigurationRequest();

        // And: configuring the dashboard without authentication
        final MockHttpServletRequestBuilder updateRequest = put(DASHBOARD_CONFIGURATION_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        // When: configuring the dashboard
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail configuring dashboard when not part of team")
    public void shouldFailConfiguringDashboardWhenNotPartOfTeam() throws Exception {
        // Given: existing teams
        final Team teamA = aValidTeam();
        final Team teamB = aValidTeam();

        // And: an existing dashboard
        final Dashboard dashboard = aValidDashboard(teamB);

        // And: authenticated user with correct role but different team
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);
        addUserToTeam(user, teamA);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: a valid configuration request
        final DashboardConfigurationRequest request = new DashboardConfigurationRequest();

        // And: configuring the dashboard
        final MockHttpServletRequestBuilder updateRequest = put(DASHBOARD_CONFIGURATION_PATH, dashboard.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        // When: configuring the dashboard
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: response should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: The reason is in the response
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), equalTo(ApiErrorCode.CANNOT_ACCESS_DASHBOARD.getReason()));
    }

    @Test
    @DisplayName("Should fail configuring non-existent dashboard")
    public void shouldFailConfiguringNonExistentDashboard() throws Exception {
        // Given: authenticated user with correct role
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: a valid configuration request
        final DashboardConfigurationRequest request = new DashboardConfigurationRequest();

        // And: configuring non-existent dashboard
        final MockHttpServletRequestBuilder updateRequest = put(DASHBOARD_CONFIGURATION_PATH, 999L)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        // When: configuring the dashboard
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: response should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: The reason is in the response
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), equalTo(ApiErrorCode.DASHBOARD_NOT_FOUND_ERROR.getReason()));
    }
}
