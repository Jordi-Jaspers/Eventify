package org.jordijaspers.eventify.api.user.controller;

import io.restassured.module.mockmvc.response.MockMvcResponse;

import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.authentication.model.response.UserDetailsResponse;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.model.request.UpdateAuthorityRequest;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class UserManagementControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should lock user successfully")
    public void shouldLockUserSuccessfully() {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithAuthority(Authority.ADMIN);

        // Given: A validated user
        final User user = aValidatedUser();

        // When: Locking the user
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + admin.getAccessToken().getValue())
            .when()
            .post(LOCK_USER_PATH, user.getId())
            .andReturn();

        // Then: Response should be successful
        response.then().statusCode(SC_OK);

        // And: Response should indicate user is locked
        response.then().body("enabled", is(false));

        // And: User should not be able to authenticate
        assertThat(userRepository.findById(user.getId()).orElseThrow().isEnabled(), is(false));
    }

    @Test
    @DisplayName("Should unlock user successfully")
    public void shouldUnlockUserSuccessfully() {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: A locked user
        final User user = aLockedUser();

        // When: Unlocking the user
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + admin.getAccessToken().getValue())
            .when()
            .post(UNLOCK_USER_PATH, user.getId())
            .andReturn();

        // Then: Response should be successful
        response.then().statusCode(SC_OK);

        // And: Response should indicate user is unlocked
        response.then().body("enabled", is(true));

        // And: User should be able to authenticate
        assertThat(userRepository.findById(user.getId()).orElseThrow().isEnabled(), is(true));
    }

    @Test
    @DisplayName("Should return error when locking non-existing user")
    public void shouldReturnErrorWhenLockingNonExistingUser() {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithAuthority(Authority.ADMIN);

        // When: Trying to lock the non-existing user
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + admin.getAccessToken().getValue())
            .when()
            .post(LOCK_USER_PATH, 99999L)
            .andReturn();

        // Then: Should return a BAD_REQUEST error
        response.then().statusCode(SC_BAD_REQUEST);

        // And: Response should contain the error message
        response.then().body("apiErrorReason", is(ApiErrorCode.USER_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should update user authority successfully")
    public void shouldUpdateUserAuthoritySuccessfully() {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: A validated user
        final User user = aValidatedUser();

        // And: An authority update request
        final UpdateAuthorityRequest request = anUpdateAuthorityRequest(Authority.ADMIN);

        // When: Updating the user's authority
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + admin.getAccessToken().getValue())
            .body(request)
            .when()
            .post(USER_PATH, user.getId())
            .andReturn();

        // Then: Response should be successful
        response.then().statusCode(SC_OK);

        // And: Response should contain updated authority
        final UserDetailsResponse userDetails = response.as(UserDetailsResponse.class);
        assertThat(userDetails.getAuthority(), is(Authority.ADMIN.getName().toUpperCase()));
    }

    @Test
    @DisplayName("Should return error when updating authority for non-existing user")
    public void shouldReturnErrorWhenUpdatingAuthorityForNonExistingUser() {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: An authority update request
        final UpdateAuthorityRequest request = anUpdateAuthorityRequest(Authority.ADMIN);

        // When: Trying to update authority of non-existing user
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + admin.getAccessToken().getValue())
            .body(request)
            .when()
            .post(USER_PATH, 99999L)
            .andReturn();

        // Then: Should return a BAD_REQUEST error
        response.then().statusCode(SC_BAD_REQUEST);

        // And: Response should contain the error message
        response.then().body("apiErrorReason", is(ApiErrorCode.USER_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should return error when updating authority without proper authorization")
    public void shouldReturnErrorWhenUpdatingAuthorityWithoutProperAuthorization() {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: An authority update request
        final UpdateAuthorityRequest request = anUpdateAuthorityRequest(Authority.ADMIN);

        // When: Trying to update authority without proper authorization
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(USER_PATH, user.getId())
            .andReturn();

        // Then: Should return an UNAUTHORIZED error
        response.then().statusCode(SC_UNAUTHORIZED);
    }
}
