package org.jordijaspers.eventify.api.check.controller;

import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.jordijaspers.eventify.api.Paths.CHECK_PATH;
import static org.jordijaspers.eventify.common.constants.Constants.Security.BEARER;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CheckController Integration Tests")
public class CheckControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return unauthorized when searching checks without authentication")
    public void shouldReturnUnauthorizedWhenSearchingWithoutAuthentication() throws Exception {
        // When: searching checks without authentication
        final MockHttpServletRequestBuilder request = get(CHECK_PATH)
            .contentType(APPLICATION_JSON)
            .queryParam("q", "test");

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return matching checks with pagination")
    public void shouldReturnMatchingChecksWithPagination() throws Exception {
        // Given: a set of checks
        final Source source = aValidSource();
        generateChecks(source, 5);

        // And: authenticated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: searching with pagination
        final MockHttpServletRequestBuilder request = get(CHECK_PATH)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .contentType(APPLICATION_JSON)
            .queryParam("q", CHECK_NAME)
            .queryParam("page", "0")
            .queryParam("size", "3");

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: response should contain paginated results
        response.andExpect(jsonPath("$.content").value(hasSize(3)));
    }

    @Test
    @DisplayName("Should return empty page when no checks match search query")
    public void shouldReturnEmptyPageWhenNoChecksMatchQuery() throws Exception {
        // Given: authenticated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: searching with a query that matches no checks
        final MockHttpServletRequestBuilder request = get(CHECK_PATH)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .contentType(APPLICATION_JSON)
            .queryParam("q", "nonexistentcheck");

        final ResultActions response = mockMvc.perform(request);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: response should contain empty page
        response.andExpect(jsonPath("$.content").isEmpty());
        response.andExpect(jsonPath("$.page.totalElements").value(0));
    }
}
