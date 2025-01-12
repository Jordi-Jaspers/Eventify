package org.jordijaspers.eventify.api.team.controller;

import io.restassured.module.mockmvc.response.MockMvcResponse;

import java.util.Set;
import java.util.stream.Collectors;

import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.team.model.request.TeamMemberRequest;
import org.jordijaspers.eventify.api.team.model.request.TeamRequest;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class TeamControllerTest extends IntegrationTest {

    @Test
    @WithMockUser(authorities = "READ_TEAMS")
    @DisplayName("Should return all teams when requested")
    public void shouldReturnAllTeamsWhenRequested() {
        // Given: Multiple teams exist
        final Team team1 = aValidTeam();
        final Team team2 = aValidTeam();

        // When: Requesting all teams
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
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
    @WithMockUser(authorities = "WRITE_TEAMS")
    @DisplayName("Should create team successfully")
    public void shouldCreateTeamSuccessfully() {
        // Given: A valid team request
        final TeamRequest request = aTeamRequest();

        // When: Creating a new team
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
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
    @WithMockUser(authorities = "WRITE_TEAMS")
    @DisplayName("Should not create team with duplicate name")
    public void shouldNotCreateTeamWithDuplicateName() {
        // Given: An existing team
        final Team existingTeam = aValidTeam();

        // And: A team request with the same name
        final TeamRequest request = aTeamRequest()
            .setName(existingTeam.getName());

        // When: Creating a team with the same name
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
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
    @WithMockUser(authorities = "WRITE_TEAMS")
    @DisplayName("Should update team successfully")
    public void shouldUpdateTeamSuccessfully() {
        // Given: An existing team
        final Team team = aValidTeam();

        // And: A team request to update the team
        final TeamRequest updateRequest = aTeamRequest();

        // When: Updating the team
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
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
    @WithMockUser(authorities = "WRITE_TEAMS")
    @DisplayName("Should delete team successfully")
    public void shouldDeleteTeamSuccessfully() {
        // Given: An existing team
        final Team team = aValidTeam();

        // When: Deleting the team
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .delete(TEAM_PATH, team.getId())
            .andReturn();

        // Then: Team should be deleted successfully
        response.then().statusCode(SC_NO_CONTENT);

        // And: Team should not exist anymore
        assertThat(teamRepository.existsById(team.getId()), is(false));
    }

    @Test
    @WithMockUser(authorities = "WRITE_TEAMS")
    @DisplayName("Should add members to team successfully")
    public void shouldAddMembersToTeamSuccessfully() {
        // Given: A team and users
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
    @WithMockUser(authorities = "WRITE_TEAMS")
    @DisplayName("Should remove members from team successfully")
    public void shouldRemoveMembersFromTeamSuccessfully() {
        // Given: A team with members
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
    @WithMockUser(authorities = "WRITE_TEAMS")
    @DisplayName("Should return an error deleting a non-existing member")
    public void shouldReturnErrorDeletingNonExistingMember() {
        // Given: A team with members
        final Team team = AValidTeamWithMembers(2);

        // And: A request to remove a non-existing member from the team
        final TeamMemberRequest request = new TeamMemberRequest()
            .setUserIds(Set.of(99999L));

        // When: Removing the non-existing member from the team
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
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
    @WithMockUser(authorities = "WRITE_TEAMS")
    @DisplayName("Should return an error for non-existent team")
    public void shouldReturnNotFoundForNonExistentTeam() {
        // Given: A non-existent team ID
        final Long nonExistentId = 99999L;

        // And: A team request
        final TeamRequest request = aTeamRequest();

        // When: Trying to update the non-existent team
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .put(TEAM_PATH, nonExistentId)
            .andReturn();

        // Then: Should return a BAD_REQUEST error
        response.then().statusCode(SC_BAD_REQUEST);

        // And: Response should contain the error message
        response.then().body("apiErrorReason", is(ApiErrorCode.TEAM_NOT_FOUND_ERROR.getReason()));
    }
}
