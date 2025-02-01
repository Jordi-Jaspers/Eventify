package org.jordijaspers.eventify.api.team.controller;

import io.restassured.module.mockmvc.response.MockMvcResponse;

import java.util.Set;
import java.util.stream.Collectors;

import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.team.model.request.TeamMemberRequest;
import org.jordijaspers.eventify.api.team.model.request.TeamRequest;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@DisplayName("TeamController Integration Tests")
public class TeamControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return unauthorized when requesting all teams without authentication")
    public void shouldReturnUnauthorizedWhenRequestedWithoutAuthentication() {
        // When: Requesting all teams without authentication
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(TEAMS_PATH)
            .andReturn();

        // Then: Response should be unauthorized
        response.then().statusCode(SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should return forbidden when requesting all teams with invalid authority")
    public void shouldReturnForbiddenWhenRequestedWithInvalidAuthority() {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.NONE);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: Requesting all teams with invalid authority
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .when()
            .get(TEAMS_PATH)
            .andReturn();

        // Then: Response should be forbidden
        response.then().statusCode(SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Should return all teams when requested")
    public void shouldReturnAllTeamsWhenRequested() {
        // Given: authenticated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: Multiple teams exist
        final Team team1 = aValidTeam();
        final Team team2 = aValidTeam();

        // When: Requesting all teams
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .when()
            .get(TEAMS_PATH)
            .andReturn();

        // Then: Response should be successful
        response.then().statusCode(SC_OK);

        // And: Response should contain all teams
        response.then().body("size()", greaterThanOrEqualTo(2));
        response.then().body("findAll { it.id == " + team1.getId() + " }.name", hasItem(team1.getName()));
        response.then().body("findAll { it.id == " + team2.getId() + " }.name", hasItem(team2.getName()));
    }

    @Test
    @DisplayName("Should create team successfully")
    public void shouldCreateTeamSuccessfully() {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: A valid team request
        final TeamRequest request = aTeamRequest();

        // When: Creating a new team
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .body(request)
            .when()
            .post(TEAMS_PATH)
            .andReturn();

        // Then: Team should be created successfully
        response.then().statusCode(SC_CREATED);

        // And: Response should contain the created team
        response.then().body("name", equalTo(request.getName()));
        response.then().body("description", equalTo(request.getDescription()));
        response.then().body("members", empty());
    }

    @Test
    @DisplayName("Should not create team with duplicate name")
    public void shouldNotCreateTeamWithDuplicateName() {
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
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .body(request)
            .when()
            .post(TEAMS_PATH)
            .andReturn();

        // Then: Should return conflict error
        response.then().statusCode(SC_BAD_REQUEST);

        // And: Response should contain the error message
        response.then().body("apiErrorReason", is(ApiErrorCode.TEAM_ALREADY_EXISTS_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should update team successfully")
    public void shouldUpdateTeamSuccessfully() {
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
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .body(updateRequest)
            .when()
            .put(TEAM_PATH, team.getId())
            .andReturn();

        // Then: Team should be updated successfully
        response.then().statusCode(SC_OK);

        // And: Response should contain the updated team
        response.then().body("name", equalTo(updateRequest.getName()));
        response.then().body("description", equalTo(updateRequest.getDescription()));
    }

    @Test
    @DisplayName("Should delete team successfully")
    public void shouldDeleteTeamSuccessfully() {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: An existing team
        final Team team = aValidTeam();

        // When: Deleting the team
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .when()
            .delete(TEAM_PATH, team.getId())
            .andReturn();

        // Then: Team should be deleted successfully
        response.then().statusCode(SC_NO_CONTENT);

        // And: Team should not exist anymore
        assertThat(teamRepository.existsById(team.getId()), is(false));
    }

    @Test
    @DisplayName("Should add members to team successfully")
    public void shouldAddMembersToTeamSuccessfully() {
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
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .body(request)
            .when()
            .put(TEAM_MEMBERS_PATH, team.getId())
            .andReturn();

        // Then: Members should be added successfully
        response.then().statusCode(SC_OK);

        // And: Response should contain the added members
        response.then().body("members.size()", equalTo(2));
        response.then().body("members.id", hasItems(user1.getId().intValue(), user2.getId().intValue()));
    }

    @Test
    @DisplayName("Should remove members from team successfully")
    public void shouldRemoveMembersFromTeamSuccessfully() {
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
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .body(request)
            .when()
            .delete(TEAM_MEMBERS_PATH, team.getId())
            .andReturn();

        // Then: Members should be removed successfully
        response.then().statusCode(SC_OK);

        // And: Response should contain the removed members
        response.then().body("members", empty());
    }

    @Test
    @DisplayName("Should return an error deleting a non-existing member")
    public void shouldReturnErrorDeletingNonExistingMember() {
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
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .body(request)
            .when()
            .delete(TEAM_MEMBERS_PATH, team.getId())
            .andReturn();

        // Then: Should return a BAD_REQUEST error
        response.then().statusCode(SC_BAD_REQUEST);

        // And: Response should contain the error message
        response.then().body("apiErrorReason", is(ApiErrorCode.USER_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should return an error for non-existent team")
    public void shouldReturnNotFoundForNonExistentTeam() {
        // Given: authenticated user
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: A team request
        final TeamRequest request = aTeamRequest();

        // When: Trying to update the non-existent team
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .body(request)
            .when()
            .put(TEAM_PATH, 99999L)
            .andReturn();

        // Then: Should return a BAD_REQUEST error
        response.then().statusCode(SC_BAD_REQUEST);

        // And: Response should contain the error message
        response.then().body("apiErrorReason", is(ApiErrorCode.TEAM_NOT_FOUND_ERROR.getReason()));
    }
}
