package io.github.eventify.api.user.controller;

import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.request.UpdateUserDetailsRequest;
import io.github.eventify.api.user.model.response.UserDetailsResponse;
import io.github.eventify.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - User Controller")
public class UserControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return user details for authenticated user")
    public void shouldReturnUserDetailsForAuthenticatedUser() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Requesting user details
        final MockHttpServletRequestBuilder request = get(USER_DETAILS)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain the user details
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);

        assertThat(userDetails.getId(), is(user.getId()));
        assertThat(userDetails.getEmail(), is(user.getEmail()));
        assertThat(userDetails.getFirstName(), is(user.getFirstName()));
        assertThat(userDetails.getLastName(), is(user.getLastName()));
        assertThat(userDetails.getPermissions(), not(empty()));
    }

    @Test
    @DisplayName("Should update user details successfully")
    public void shouldUpdateUserDetailsSuccessfully() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Update details request
        final UpdateUserDetailsRequest request = anUpdateUserDetailsRequest();

        // When: Updating user details
        final MockHttpServletRequestBuilder updateRequest = post(USER_DETAILS)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain updated user details
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);

        assertThat(userDetails.getId(), is(user.getId()));
        assertThat(userDetails.getFirstName(), is(request.getFirstName()));
        assertThat(userDetails.getLastName(), is(request.getLastName()));
    }

}
