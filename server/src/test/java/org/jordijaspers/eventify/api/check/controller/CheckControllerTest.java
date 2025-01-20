package org.jordijaspers.eventify.api.check.controller;

import io.restassured.module.mockmvc.response.MockMvcResponse;

import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.CHECK_PATH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@DisplayName("CheckController Integration Tests")
public class CheckControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return unauthorized when searching checks without authentication")
    public void shouldReturnUnauthorizedWhenSearchingWithoutAuthentication() {
        // When: searching checks without authentication
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .queryParam("q", "test")
            .when()
            .get(CHECK_PATH)
            .andReturn();

        // Then: response should be unauthorized
        response.then().statusCode(SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should return matching checks with pagination")
    public void shouldReturnMatchingChecksWithPagination() {
        // Given: a set of checks
        final Source source = aValidSource();
        generateChecks(source, 5);

        // And: authenticated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: searching with pagination
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .queryParam("q", CHECK_NAME)
            .queryParam("page", 0)
            .queryParam("size", 3)
            .when()
            .get(CHECK_PATH)
            .andReturn();

        // Then: response should be successful
        response.then().statusCode(SC_OK);

        // And: response should contain paginated results
        response.then().body("content", hasSize(3));
        response.then().body("page.totalElements", equalTo(5));
    }

    @Test
    @DisplayName("Should return empty page when no checks match search query")
    public void shouldReturnEmptyPageWhenNoChecksMatchQuery() {
        // Given: authenticated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: searching with a query that matches no checks
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .queryParam("q", "nonexistentcheck")
            .when()
            .get(CHECK_PATH)
            .andReturn();

        // Then: response should be successful
        response.then().statusCode(SC_OK);

        // And: response should contain empty page
        response.then().body("content", empty());
        response.then().body("page.totalElements", equalTo(0));
    }
}
