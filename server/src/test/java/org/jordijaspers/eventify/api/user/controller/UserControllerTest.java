package org.jordijaspers.eventify.api.user.controller;

import io.restassured.module.mockmvc.response.MockMvcResponse;

import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.model.request.UpdateEmailRequest;
import org.jordijaspers.eventify.api.user.model.request.UpdateUserDetailsRequest;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class UserControllerTest extends IntegrationTest {

    @Test
    @WithMockUser(authorities = "READ_USERS")
    @DisplayName("Should return all users when requested")
    public void shouldReturnAllUsersWhenRequested() {
        // Given: Multiple users exist
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // When: Requesting all users
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(USERS_PATH)
            .andReturn();

        // Then: Response should be successful
        response.then().statusCode(SC_OK);

        // And: Response should contain all users
        response.then().body("size()", greaterThanOrEqualTo(2));
        response.then().body("findAll { it.id == " + user1.getId() + " }.email", hasItem(user1.getEmail()));
        response.then().body("findAll { it.id == " + user2.getId() + " }.email", hasItem(user2.getEmail()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return user details for authenticated user")
    public void shouldReturnUserDetailsForAuthenticatedUser() {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Requesting user details
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + user.getAccessToken().getValue())
            .when()
            .get(USER_DETAILS)
            .andReturn();

        // Then: Response should be successful
        response.then().statusCode(SC_OK);

        // And: Response should contain the user details
        response.then().body("email", equalTo(user.getEmail()));
        response.then().body("firstName", equalTo(user.getFirstName()));
        response.then().body("lastName", equalTo(user.getLastName()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should update user details successfully")
    public void shouldUpdateUserDetailsSuccessfully() {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Update details request
        final UpdateUserDetailsRequest request = anUpdateUserDetailsRequest();

        // When: Updating user details
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + user.getAccessToken().getValue())
            .body(request)
            .when()
            .post(USER_DETAILS)
            .andReturn();

        // Then: Response should be successful
        response.then().statusCode(SC_OK);

        // And: Response should contain updated user details
        response.then().body("firstName", equalTo(request.getFirstName()));
        response.then().body("lastName", equalTo(request.getLastName()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should update user email successfully")
    public void shouldUpdateUserEmailSuccessfully() {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Update email request
        final UpdateEmailRequest request = anUpdateEmailRequest();

        // When: Updating user email
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + user.getAccessToken().getValue())
            .body(request)
            .when()
            .post(USER_UPDATE_EMAIL_PATH)
            .andReturn();

        // Then: Response should be successful
        response.then().statusCode(SC_OK);

        // And: Response should contain updated email
        response.then().body("email", equalTo(request.getEmail()));
        response.then().body("validated", is(false));
    }

    @Test
    @WithMockUser
    @DisplayName("Should not update email when email is already in use")
    public void shouldNotUpdateEmailWhenEmailIsAlreadyInUse() {
        // Given: Two validated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: Update email request with existing email
        final UpdateEmailRequest request = new UpdateEmailRequest()
            .setEmail(user2.getEmail());

        // When: Updating user email
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + user1.getAccessToken().getValue())
            .body(request)
            .when()
            .post(USER_UPDATE_EMAIL_PATH)
            .andReturn();

        // Then: Should return a BAD_REQUEST error
        response.then().statusCode(SC_BAD_REQUEST);

        // And: Response should contain the error message
        response.then().body("apiErrorReason", is(ApiErrorCode.USER_ALREADY_EXISTS_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should validate email successfully")
    public void shouldValidateEmailSuccessfully() {
        // Given: An email validation request
        final UpdateEmailRequest request = anUpdateEmailRequest();

        // When: Validating email
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(PUBLIC_VALIDATE_EMAIL_PATH)
            .andReturn();

        // Then: Response should be successful
        response.then().statusCode(SC_OK);

        // And: Response should indicate email is valid and not in use
        response.then().body(equalTo("false"));
    }

    @Test
    @DisplayName("Should indicate when email is already in use during validation")
    public void shouldIndicateWhenEmailIsAlreadyInUseValidation() {
        // Given: An existing validated user
        final User user = aValidatedUser();

        // And: An email validation request with existing email
        final UpdateEmailRequest request = new UpdateEmailRequest()
            .setEmail(user.getEmail());

        // When: Validating email
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(PUBLIC_VALIDATE_EMAIL_PATH)
            .andReturn();

        // Then: Response should be successful
        response.then().statusCode(SC_OK);

        // And: Response should indicate is invalid - email is already in use
        response.then().body(equalTo("false"));
    }
}
