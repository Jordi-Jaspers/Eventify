package org.jordijaspers.eventify.api.event.controller;

import java.time.ZonedDateTime;
import jakarta.servlet.http.Cookie;

import org.hawaiiframework.web.resource.ApiErrorResponseResource;
import org.hawaiiframework.web.resource.ValidationErrorResponseResource;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.event.model.Status;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.source.model.ApiKey;
import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.EVENTS_PATH;
import static org.jordijaspers.eventify.common.constants.Constants.Security.*;
import static org.jordijaspers.eventify.common.security.filter.ApiKeyAuthenticationFilter.API_KEY_HEADER;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.fromJson;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.toJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("EventController Integration Tests")
public class EventControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return unauthorized when submitting event without authentication")
    public void shouldReturnUnauthorizedWhenSubmittedWithoutAuthentication() throws Exception {
        // Given: a valid event request
        final EventRequest request = anEventRequest(1L, Status.OK);

        // And: submitting event without authentication
        final MockHttpServletRequestBuilder submitRequest = post(EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        // When: submitting the event
        final ResultActions response = mockMvc.perform(submitRequest);

        // Then: response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return forbidden when submitting event with incorrect role")
    public void shouldReturnForbiddenWhenSubmittedWithIncorrectRole() throws Exception {
        // Given: authenticated user with incorrect role
        final User user = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: a valid event request
        final EventRequest request = anEventRequest(1L, Status.OK);

        // And: submitting event
        final MockHttpServletRequestBuilder submitRequest = post(EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        // When: submitting the event
        final ResultActions response = mockMvc.perform(submitRequest);

        // Then: response should be bad request
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should submit event successfully with valid API key")
    public void shouldSubmitEventSuccessfullyWhenAuthenticated() throws Exception {
        // Given: existing source and check
        final Source source = aValidSource();
        final Check check = aValidCheck(source);

        // And: The Api key  which is allowed to communicate with the source
        final ApiKey apiKey = source.getApiKey();

        // And: a valid event request
        final EventRequest request = anEventRequest(check.getId(), Status.OK);

        // And: submitting event
        final MockHttpServletRequestBuilder submitRequest = post(EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        // When: submitting the event
        final ResultActions response = mockMvc.perform(submitRequest);

        // Then: response should be accepted
        response.andExpect(status().is(SC_ACCEPTED));
    }


    @Test
    @DisplayName("Should submit event successfully with valid API key and cookies present")
    public void shouldSubmitEventSuccessfullyWhenAuthenticatedAndCookiesPresent() throws Exception {
        // Given: A logged-in user with valid cookies
        final User user = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: The user has valid access and refresh tokens
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        final Token refreshToken = user.getRefreshToken();
        assertThat(refreshToken.getValue(), notNullValue());

        // And: existing source and check
        final Source source = aValidSource();
        final Check check = aValidCheck(source);

        // And: The Api key  which is allowed to communicate with the source
        final ApiKey apiKey = source.getApiKey();

        // And: a valid event request
        final EventRequest request = anEventRequest(check.getId(), Status.OK);

        // And: submitting event
        final MockHttpServletRequestBuilder submitRequest = post(EVENTS_PATH)
            .cookie(new Cookie(ACCESS_TOKEN_COOKIE, accessToken.getValue()))
            .cookie(new Cookie(REFRESH_TOKEN_COOKIE, refreshToken.getValue()))
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        // When: submitting the event
        final ResultActions response = mockMvc.perform(submitRequest);

        // Then: response should be accepted
        response.andExpect(status().is(SC_ACCEPTED));
    }

    @Test
    @DisplayName("Should reject event with an API key that is not allowed to communicate with the source")
    public void shouldRejectEventWithInvalidApiKey() throws Exception {
        // Given: existing source and check
        final Source source = aValidSource();
        final Check check = aValidCheck(source);

        // And: Another source with a different API key
        final Source anotherSource = aValidSource();

        // And: The Api key  which is not allowed to communicate with the source
        final ApiKey apiKey = anotherSource.getApiKey();

        // And: a valid event request
        final EventRequest request = anEventRequest(check.getId(), Status.OK);

        // And: submitting event
        final MockHttpServletRequestBuilder submitRequest = post(EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        // When: submitting the event
        final ResultActions response = mockMvc.perform(submitRequest);

        // Then: response should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: response should contain not found error
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), equalTo(ApiErrorCode.CANNOT_ACCESS_CHECK.getReason()));
    }

    @Test
    @DisplayName("Should reject event with validation errors")
    public void shouldRejectEventWithValidationErrors() throws Exception {
        // Given: A valid source and check
        final Source source = aValidSource();
        final Check check = aValidCheck(source);

        // And: The Api key  which is allowed to communicate with the source
        final ApiKey apiKey = source.getApiKey();

        // And: an invalid event request
        final EventRequest request = anEventRequest(check.getId(), null)
            .setMessage(null)
            .setTimestamp(ZonedDateTime.now().plusDays(1));

        // And: submitting event
        final MockHttpServletRequestBuilder submitRequest = post(EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        // When: submitting the event
        final ResultActions response = mockMvc.perform(submitRequest);

        // Then: response should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: response should contain validation errors
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource resource = fromJson(content, ValidationErrorResponseResource.class);
        assertThat(resource.getErrors(), hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("Should reject event for non-existent check")
    public void shouldRejectEventForNonExistentCheck() throws Exception {
        // Given: A valid Source and Check.
        final Source source = aValidSource();
        aValidCheck(source);

        // And: The Api key  which is allowed to communicate with the source
        final ApiKey apiKey = source.getApiKey();

        // And: an event request for non-existent check
        final EventRequest request = anEventRequest(999L, Status.OK);

        // And: submitting event
        final MockHttpServletRequestBuilder submitRequest = post(EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        // When: submitting the event
        final ResultActions response = mockMvc.perform(submitRequest);

        // Then: response should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: response should contain not found error
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), equalTo(ApiErrorCode.CANNOT_ACCESS_CHECK.getReason()));
    }
}
