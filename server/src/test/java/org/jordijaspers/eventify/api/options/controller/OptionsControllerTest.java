package org.jordijaspers.eventify.api.options.controller;

import java.util.Map;

import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.type.TypeReference;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.jordijaspers.eventify.api.Paths.OPTIONS_PATH;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.fromJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("OptionsController Integration Tests")
public class OptionsControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return unauthorized when requesting all options without authentication")
    public void shouldReturnUnauthorizedWhenRequestedWithoutAuthentication() throws Exception {
        // Given: The request to get all options
        final MockHttpServletRequestBuilder request = get(OPTIONS_PATH)
            .contentType(APPLICATION_JSON);

        // When: Requesting all constants for the options menu without authentication
        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("should return all available options with correct status")
    public void shouldReturnAllOptionsWhenRequested() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // When: Requesting all constants for the options menu
        final MockHttpServletRequestBuilder request = get(OPTIONS_PATH)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .contentType(APPLICATION_JSON);

        // And: Making the request
        final ResultActions response = mockMvc.perform(request);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: response should contain all options
        final String responseContent = response.andReturn().getResponse().getContentAsString();
        final Map<String, Object> options = fromJson(responseContent, new TypeReference<>() {});

        // And: response should contain all options
        options.keySet().forEach(key -> {
            assertThat(key, oneOf("authorities"));
        });
    }
}
