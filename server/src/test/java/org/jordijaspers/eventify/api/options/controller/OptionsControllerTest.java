package org.jordijaspers.eventify.api.options.controller;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.response.MockMvcResponse;

import java.util.Map;

import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.jordijaspers.eventify.api.Paths.OPTIONS_PATH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@DisplayName("OptionsController Integration Tests")
public class OptionsControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return unauthorized when requesting all options without authentication")
    public void shouldReturnUnauthorizedWhenRequestedWithoutAuthentication() {
        // When: requesting all constants for the options menu without authentication
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(OPTIONS_PATH)
            .andReturn();

        // Then: response should be unauthorized
        response.then().statusCode(SC_UNAUTHORIZED);
    }


    @Test
    @DisplayName("should return all available options with correct status")
    public void shouldReturnAllOptionsWhenRequested() {
        // Given: authenticated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: requesting all constants for the options menu
        final MockMvcResponse response = given()
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(OPTIONS_PATH)
            .andReturn();

        // Then: response should be successful
        response.then().statusCode(SC_OK);

        // And: response should contain all options
        final Map<String, Object> options = response.body().as(new TypeRef<>() {
        });

        // And: response should contain all options
        options.keySet().forEach(key -> {
            assertThat(key, oneOf("authorities"));
        });
    }
}
