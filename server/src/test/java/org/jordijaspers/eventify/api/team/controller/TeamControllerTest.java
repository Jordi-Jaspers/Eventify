package org.jordijaspers.eventify.api.team.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawaiiframework.web.resource.ApiErrorResponseResource;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.team.model.request.TeamMemberRequest;
import org.jordijaspers.eventify.api.team.model.request.TeamRequest;
import org.jordijaspers.eventify.api.team.model.response.TeamResponse;
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
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.jordijaspers.eventify.common.constants.Constants.Security.BEARER;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.fromJson;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.toJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("TeamController Integration Tests")
public class TeamControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return unauthorized when requesting all teams without authentication")
    public void shouldReturnUnauthorizedWhenRequestedWithoutAuthentication() throws Exception {
        // When: Requesting all teams without authentication
        final MockHttpServletRequestBuilder request = get(TEAMS_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return forbidden when requesting all teams with invalid authority")
    public void shouldReturnForbiddenWhenRequestedWithInvalidAuthority() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.NONE);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: Requesting all teams with invalid authority
        final MockHttpServletRequestBuilder request = get(TEAMS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return all teams when requested")
    public void shouldReturnAllTeamsWhenRequested() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: Multiple teams exist
        final Team team1 = aValidTeam();
        final Team team2 = aValidTeam();

        // When: Requesting all teams
        final MockHttpServletRequestBuilder request = get(TEAMS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain all teams
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<TeamResponse> teams = fromJson(content, new TypeReference<>() {});

        assertThat(teams.size(), greaterThanOrEqualTo(2));
        assertThat(
            teams.stream()
                .filter(t -> t.getId().equals(team1.getId()))
                .map(TeamResponse::getName)
                .findFirst()
                .orElse(null),
            equalTo(team1.getName())
        );
        assertThat(
            teams.stream()
                .filter(t -> t.getId().equals(team2.getId()))
                .map(TeamResponse::getName)
                .findFirst()
                .orElse(null),
            equalTo(team2.getName())
        );
    }

    @Test
    @DisplayName("Should create team successfully")
    public void shouldCreateTeamSuccessfully() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: A valid team request
        final TeamRequest request = aTeamRequest();

        // When: Creating a new team
        final MockHttpServletRequestBuilder createRequest = post(TEAMS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Team should be created successfully
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain the created team
        final String content = response.andReturn().getResponse().getContentAsString();
        final TeamResponse team = fromJson(content, TeamResponse.class);

        assertThat(team.getName(), equalTo(request.getName()));
        assertThat(team.getDescription(), equalTo(request.getDescription()));
        assertThat(team.getMembers(), empty());
    }

    @Test
    @DisplayName("Should not create team with duplicate name")
    public void shouldNotCreateTeamWithDuplicateName() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: An existing team
        final Team existingTeam = aValidTeam();

        // And: A team request with the same name
        final TeamRequest request = aTeamRequest()
            .setName(existingTeam.getName());

        // When: Creating a team with the same name
        final MockHttpServletRequestBuilder createRequest = post(TEAMS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Should return conflict error
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain the error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.TEAM_ALREADY_EXISTS_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should update team successfully")
    public void shouldUpdateTeamSuccessfully() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: An existing team
        final Team team = aValidTeam();

        // And: A team request to update the team
        final TeamRequest updateRequest = aTeamRequest();

        // When: Updating the team
        final MockHttpServletRequestBuilder request = put(TEAM_PATH, team.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(request);

        // Then: Team should be updated successfully
        response.andExpect(status().is(SC_OK));

        // And: Response should contain the updated team
        final String content = response.andReturn().getResponse().getContentAsString();
        final TeamResponse updatedTeam = fromJson(content, TeamResponse.class);

        assertThat(updatedTeam.getName(), equalTo(updateRequest.getName()));
        assertThat(updatedTeam.getDescription(), equalTo(updateRequest.getDescription()));
    }

    @Test
    @DisplayName("Should delete team successfully")
    public void shouldDeleteTeamSuccessfully() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: An existing team
        final Team team = aValidTeam();

        // When: Deleting the team
        final MockHttpServletRequestBuilder request = delete(TEAM_PATH, team.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Team should be deleted successfully
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: Team should not exist anymore
        assertThat(teamRepository.existsById(team.getId()), is(false));
    }

    @Test
    @DisplayName("Should add members to team successfully")
    public void shouldAddMembersToTeamSuccessfully() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: A team and users
        final Team team = aValidTeam();

        // And: Two validated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: A request to add members to the team
        final TeamMemberRequest request = new TeamMemberRequest()
            .setUserIds(Set.of(user1.getId(), user2.getId()));

        // When: Adding members to the team
        final MockHttpServletRequestBuilder updateRequest = put(TEAM_MEMBERS_PATH, team.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Members should be added successfully
        response.andExpect(status().is(SC_OK));

        // And: Response should contain the added members
        final String content = response.andReturn().getResponse().getContentAsString();
        final TeamResponse updatedTeam = fromJson(content, TeamResponse.class);

        assertThat(updatedTeam.getMembers().size(), equalTo(2));
        assertThat(
            updatedTeam.getMembers().stream()
                .map(member -> member.getId().intValue())
                .collect(Collectors.toSet()),
            hasItems(user1.getId().intValue(), user2.getId().intValue())
        );
    }

    @Test
    @DisplayName("Should remove members from team successfully")
    public void shouldRemoveMembersFromTeamSuccessfully() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: A team with members
        final Team team = AValidTeamWithMembers(2);
        final Set<Long> memberIds = team.getMembers().stream()
            .map(User::getId)
            .collect(Collectors.toSet());

        // And: A request to remove members from the team
        final TeamMemberRequest request = new TeamMemberRequest()
            .setUserIds(memberIds);

        // When: Removing members from the team
        final MockHttpServletRequestBuilder deleteRequest = delete(TEAM_MEMBERS_PATH, team.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Members should be removed successfully
        response.andExpect(status().is(SC_OK));

        // And: Response should contain the removed members
        final String content = response.andReturn().getResponse().getContentAsString();
        final TeamResponse updatedTeam = fromJson(content, TeamResponse.class);
        assertThat(updatedTeam.getMembers(), empty());
    }

    @Test
    @DisplayName("Should return an error deleting a non-existing member")
    public void shouldReturnErrorDeletingNonExistingMember() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: A team with members
        final Team team = AValidTeamWithMembers(2);

        // And: A request to remove a non-existing member from the team
        final TeamMemberRequest request = new TeamMemberRequest()
            .setUserIds(Set.of(99999L));

        // When: Removing the non-existing member from the team
        final MockHttpServletRequestBuilder deleteRequest = delete(TEAM_MEMBERS_PATH, team.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Should return a BAD_REQUEST error
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain the error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.USER_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should return an error for non-existent team")
    public void shouldReturnNotFoundForNonExistentTeam() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: A team request
        final TeamRequest request = aTeamRequest();

        // When: Trying to update the non-existent team
        final MockHttpServletRequestBuilder updateRequest = put(TEAM_PATH, 99999L)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Should return a BAD_REQUEST error
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain the error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.TEAM_NOT_FOUND_ERROR.getReason()));
    }
}
