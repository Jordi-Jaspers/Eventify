package org.jordijaspers.eventify.api.source.controller;

import java.util.List;

import org.hawaiiframework.web.resource.ApiErrorResponseResource;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.source.model.request.SourceRequest;
import org.jordijaspers.eventify.api.source.model.response.DetailedSourceResponse;
import org.jordijaspers.eventify.api.source.model.response.SourceResponse;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.type.TypeReference;

import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.jordijaspers.eventify.common.constants.Constants.Security.BEARER;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.fromJson;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.toJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SourceController Integration Tests")
public class SourceControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return unauthorized when requesting sources without authentication")
    public void shouldReturnUnauthorizedWhenRequestedWithoutAuthentication() throws Exception {
        // Given: requesting sources without authentication
        final MockHttpServletRequestBuilder request = get(SOURCES_PATH)
            .contentType(APPLICATION_JSON);

        // When: requesting sources
        final ResultActions response = mockMvc.perform(request);

        // Then: response should be unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return forbidden when requesting sources with incorrect role")
    public void shouldReturnForbiddenWhenRequestedWithIncorrectRole() throws Exception {
        // Given: authenticated user with incorrect role
        final User user = aValidatedUserWithAuthority(Authority.NONE);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: requesting sources
        final MockHttpServletRequestBuilder request = get(SOURCES_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: requesting sources
        final ResultActions response = mockMvc.perform(request);

        // Then: response should be forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return sources when authenticated with correct role")
    public void shouldReturnSourcesWhenAuthenticatedWithCorrectRole() throws Exception {
        // Given: existing sources
        final Source source = aValidSource();

        // And: authenticated user with correct role
        final User user = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: requesting sources
        final MockHttpServletRequestBuilder request = get(SOURCES_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: requesting sources
        final ResultActions response = mockMvc.perform(request);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: response should contain sources
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<SourceResponse> sources = fromJson(content, new TypeReference<>() {});
        assertThat(sources, hasSize(greaterThanOrEqualTo(1)));

        // And: response should contain the existing source
        final SourceResponse sourceResponse = sources.stream()
            .filter(s -> s.getId().equals(source.getId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Source not found in response"));
    }

    @Test
    @DisplayName("Should create source successfully when authenticated with correct role")
    public void shouldCreateSourceSuccessfullyWhenAuthenticated() throws Exception {
        // Given: authenticated user with correct role
        final User user = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: a valid source request
        final SourceRequest request = new SourceRequest()
            .setName(SOURCE_NAME)
            .setDescription("Test Source Description");

        // And: creating a source
        final MockHttpServletRequestBuilder createRequest = post(SOURCES_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        // When: creating the source
        final ResultActions response = mockMvc.perform(createRequest);

        // Then: response should be successful
        response.andExpect(status().is(SC_CREATED));

        // And: response should contain created source
        final String content = response.andReturn().getResponse().getContentAsString();
        final DetailedSourceResponse sourceResponse = fromJson(content, DetailedSourceResponse.class);
        assertThat(sourceResponse.getName(), equalTo(request.getName()));
        assertThat(sourceResponse.getDescription(), equalTo(request.getDescription()));
        assertThat(sourceResponse.getApiKey().getKey(), notNullValue());
    }

    @Test
    @DisplayName("Should return error creating a Source with incorrect role")
    public void shouldReturnErrorCreatingSourceWithIncorrectRole() throws Exception {
        // Given: authenticated user with correct role
        final User user = aValidatedUserWithAuthority(Authority.NONE);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: a valid source request
        final SourceRequest request = new SourceRequest()
            .setName(SOURCE_NAME)
            .setDescription("Test Source Description");

        // And: creating a source
        final MockHttpServletRequestBuilder createRequest = post(SOURCES_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        // When: creating the source
        final ResultActions response = mockMvc.perform(createRequest);

        // Then: response should be bad request
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should delete source successfully when authenticated with correct role")
    public void shouldDeleteSourceSuccessfullyWhenAuthenticated() throws Exception {
        // Given: an existing source
        final Source source = aValidSource();

        // And: authenticated user with correct role
        final User user = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: deleting the source
        final MockHttpServletRequestBuilder deleteRequest = delete(SOURCE_PATH, source.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: deleting the source
        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: response should be successful
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: the source should be deleted
        final Source deletedSource = sourceRepository.findById(source.getId()).orElse(null);
        assertThat(deletedSource, nullValue());
    }

    @Test
    @DisplayName("Should not return error when deleting non-existent source")
    public void shouldNotReturnErrorWhenDeletingNonExistentSource() throws Exception {
        // Given: authenticated user with correct role
        final User user = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: deleting non-existent source
        final MockHttpServletRequestBuilder deleteRequest = delete(SOURCE_PATH, 999L)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: deleting the source
        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: response should be no content
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should lock API key successfully when authenticated with correct role")
    public void shouldLockApiKeySuccessfullyWhenAuthenticated() throws Exception {
        // Given: an existing source
        final Source source = aValidSource();

        // And: authenticated user with correct role
        final User user = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: locking the API key
        final MockHttpServletRequestBuilder lockRequest = post(LOCK_API_KEY_PATH, source.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: locking the API key
        final ResultActions response = mockMvc.perform(lockRequest);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: response should contain updated source with locked API key
        final String content = response.andReturn().getResponse().getContentAsString();
        final DetailedSourceResponse sourceResponse = fromJson(content, DetailedSourceResponse.class);
        assertThat(sourceResponse.getId(), equalTo(source.getId()));
        assertThat(sourceResponse.getApiKey().isEnabled(), equalTo(false));
    }

    @Test
    @DisplayName("Should return error when locking non-existent API key")
    public void shouldReturnErrorWhenLockingNonExistentApiKey() throws Exception {
        // Given: authenticated user with correct role
        final User user = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: locking non-existent API key
        final MockHttpServletRequestBuilder lockRequest = post(LOCK_API_KEY_PATH, 999L)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: locking the API key
        final ResultActions response = mockMvc.perform(lockRequest);

        // Then: response should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: response should contain error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), equalTo(ApiErrorCode.SOURCE_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should unlock API key successfully when authenticated with correct role")
    public void shouldUnlockApiKeySuccessfullyWhenAuthenticated() throws Exception {
        // Given: an existing source with locked API key
        final Source source = aValidSource();
        source.setApiKeyEnabled(false);
        sourceRepository.save(source);

        // And: authenticated user with correct role
        final User user = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: unlocking the API key
        final MockHttpServletRequestBuilder unlockRequest = post(UNLOCK_API_KEY_PATH, source.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: unlocking the API key
        final ResultActions response = mockMvc.perform(unlockRequest);

        // Then: response should be successful
        response.andExpect(status().is(SC_OK));

        // And: response should contain updated source with unlocked API key
        final String content = response.andReturn().getResponse().getContentAsString();
        final DetailedSourceResponse sourceResponse = fromJson(content, DetailedSourceResponse.class);
        assertThat(sourceResponse.getId(), equalTo(source.getId()));
        assertThat(sourceResponse.getApiKey().isEnabled(), equalTo(true));
    }

    @Test
    @DisplayName("Should return error when unlocking non-existent API key")
    public void shouldReturnErrorWhenUnlockingNonExistentApiKey() throws Exception {
        // Given: authenticated user with correct role
        final User user = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: unlocking non-existent API key
        final MockHttpServletRequestBuilder unlockRequest = post(UNLOCK_API_KEY_PATH, 999L)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue());

        // When: unlocking the API key
        final ResultActions response = mockMvc.perform(unlockRequest);

        // Then: response should be bad request
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: response should contain error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), equalTo(ApiErrorCode.SOURCE_NOT_FOUND_ERROR.getReason()));
    }
}
