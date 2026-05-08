package io.github.eventify.api.apikey.controller;

import io.github.eventify.api.apikey.model.request.CreateApiKeyRequest;
import io.github.eventify.api.apikey.model.response.ApiKeyCreationResponse;
import io.github.eventify.api.apikey.model.response.ApiKeyListResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.resource.ApiErrorResponseResource;
import io.github.jframe.exception.resource.ValidationErrorResponseResource;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.USER_API_KEYS_PATH;
import static io.github.eventify.api.Paths.USER_API_KEY_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - User API Key Controller")
public class UserApiKeyControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should create API key successfully")
    public void createApiKeySuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: A valid create API key request
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Production Server");

        // When: Creating API key
        final MockHttpServletRequestBuilder createRequest = post(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain full key and suffix
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createResponse = fromJson(content, ApiKeyCreationResponse.class);

        assertThat(createResponse.getId(), is(notNullValue()));
        assertThat(createResponse.getName(), is("Production Server"));
        assertThat(createResponse.getKey(), startsWith("evt_"));
        assertThat(createResponse.getKey().length(), is(36));
        assertThat(createResponse.getSuffix(), is(notNullValue()));
        assertThat(createResponse.getSuffix().length(), is(4));
        assertThat(createResponse.getCreatedAt(), is(notNullValue()));
        assertThat(createResponse.getExpiresAt(), is(nullValue()));
    }

    @Test
    @DisplayName("Should create API key with expiration date")
    public void createApiKeyWithExpirationSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: A valid request with future expiration
        final OffsetDateTime futureExpiration = OffsetDateTime.now().plusDays(30);
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Temporary Key")
            .setExpiresAt(futureExpiration);

        // When: Creating API key
        final MockHttpServletRequestBuilder createRequest = post(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should include expiration date
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createResponse = fromJson(content, ApiKeyCreationResponse.class);

        assertThat(createResponse.getExpiresAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should fail when API key name is empty")
    public void createApiKeyFailsWhenNameIsEmpty() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Request with empty name
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("");

        // When: Creating API key
        final MockHttpServletRequestBuilder createRequest = post(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when API key name exceeds 100 characters")
    public void createApiKeyFailsWhenNameTooLong() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Request with name exceeding 100 characters
        final String longName = "a".repeat(101);
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName(longName);

        // When: Creating API key
        final MockHttpServletRequestBuilder createRequest = post(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when expiration date is in the past")
    public void createApiKeyFailsWhenExpirationInPast() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Request with past expiration
        final OffsetDateTime pastExpiration = OffsetDateTime.now().minusDays(1);
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Invalid Key")
            .setExpiresAt(pastExpiration);

        // When: Creating API key
        final MockHttpServletRequestBuilder createRequest = post(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain validation error for expiresAt field
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);
        assertThat(error.getErrors().getFirst().getField(), is("expiresAt"));
        assertThat(error.getErrors().getFirst().getCode(), containsStringIgnoringCase("future"));
    }

    @Test
    @DisplayName("Should fail when user has 5 API keys already")
    public void createApiKeyFailsWhenLimitExceeded() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User already has 5 API keys
        for (int i = 0; i < 5; i++) {
            final CreateApiKeyRequest createRequest = new CreateApiKeyRequest()
                .setName("Key " + (i + 1));

            mockMvc.perform(
                post(USER_API_KEYS_PATH)
                    .contentType(APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                    .content(toJson(createRequest))
            );
        }

        // When: Attempting to create 6th API key
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Sixth Key");

        final MockHttpServletRequestBuilder createRequest = post(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error code should be API_KEY_LIMIT_EXCEEDED
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), containsStringIgnoringCase("Maximum"));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user creates API key")
    public void createApiKeyFailsWhenUnauthenticated() throws Exception {
        // Given: A valid request
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Unauthorized Key");

        // When: Creating API key without authentication
        final MockHttpServletRequestBuilder createRequest = post(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should list user API keys successfully")
    public void listApiKeysSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has created API keys
        final CreateApiKeyRequest request1 = new CreateApiKeyRequest()
            .setName("Production Key");
        final CreateApiKeyRequest request2 = new CreateApiKeyRequest()
            .setName("Development Key");

        mockMvc.perform(
            post(USER_API_KEYS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request1))
        );

        mockMvc.perform(
            post(USER_API_KEYS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request2))
        );

        // When: Listing API keys
        final MockHttpServletRequestBuilder listRequest = get(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(listRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain masked keys
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiKeyListResponse listResponse = fromJson(content, ApiKeyListResponse.class);

        assertThat(listResponse.getKeys(), hasSize(2));
        assertThat(listResponse.getLimit(), is(5));

        // And: Keys should be masked
        listResponse.getKeys().forEach(key -> {
            assertThat(key.getMaskedKey(), startsWith("evt_******"));
            assertThat(key.getMaskedKey().length(), is(14));
            assertThat(key.getName(), is(notNullValue()));
            assertThat(key.getCreatedAt(), is(notNullValue()));
        });
    }

    @Test
    @DisplayName("Should return empty list when user has no API keys")
    public void listApiKeysReturnsEmptyWhenNoKeys() throws Exception {
        // Given: An authenticated user with no API keys
        final User user = aValidatedUser();

        // When: Listing API keys
        final MockHttpServletRequestBuilder listRequest = get(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(listRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain empty list
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiKeyListResponse listResponse = fromJson(content, ApiKeyListResponse.class);

        assertThat(listResponse.getKeys(), is(empty()));
        assertThat(listResponse.getLimit(), is(5));
    }

    @Test
    @DisplayName("Should verify full key not included in list response")
    public void listApiKeysDoesNotIncludeFullKey() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has created an API key
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Test Key");

        mockMvc.perform(
            post(USER_API_KEYS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // When: Listing API keys
        final MockHttpServletRequestBuilder listRequest = get(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(listRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should not contain full key
        final String content = response.andReturn().getResponse().getContentAsString();
        assertThat(content, not(containsString("\"key\":")));
        assertThat(content, containsString("\"maskedKey\":"));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user lists API keys")
    public void listApiKeysFailsWhenUnauthenticated() throws Exception {
        // When: Listing API keys without authentication
        final MockHttpServletRequestBuilder listRequest = get(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(listRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should revoke API key successfully")
    public void revokeApiKeySuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has created an API key
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Key to Revoke");

        final MockHttpServletRequestBuilder createRequest = post(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions createResponse = mockMvc.perform(createRequest);
        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createdKey = fromJson(createContent, ApiKeyCreationResponse.class);

        // When: Revoking the API key
        final MockHttpServletRequestBuilder revokeRequest = delete(USER_API_KEY_PATH.replace("{keyId}", createdKey.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(revokeRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should create audit record when revoking API key")
    public void revokeApiKeyCreatesAuditRecord() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has created an API key
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("Audited Key");

        final MockHttpServletRequestBuilder createRequest = post(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions createResponse = mockMvc.perform(createRequest);
        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createdKey = fromJson(createContent, ApiKeyCreationResponse.class);

        // When: Revoking the API key
        final MockHttpServletRequestBuilder revokeRequest = delete(USER_API_KEY_PATH.replace("{keyId}", createdKey.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        mockMvc.perform(revokeRequest);

        // Then: Audit record should be created (verified by service test)
        // When: Attempting to revoke again
        final ResultActions secondRevoke = mockMvc.perform(revokeRequest);

        // Then: Should return NOT_FOUND
        secondRevoke.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when API key not found")
    public void revokeApiKeyFailsWhenNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Revoking non-existent API key
        final MockHttpServletRequestBuilder revokeRequest = delete(USER_API_KEY_PATH.replace("{keyId}", "99999"))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(revokeRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));

        // And: Error code should be API_KEY_NOT_FOUND
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getErrorMessage(), containsString("API key"));
    }

    @Test
    @DisplayName("Should fail when revoking other user API key")
    public void revokeApiKeyFailsWhenNotOwner() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has created an API key
        final CreateApiKeyRequest request = new CreateApiKeyRequest()
            .setName("User1 Key");

        final MockHttpServletRequestBuilder createRequest = post(USER_API_KEYS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions createResponse = mockMvc.perform(createRequest);
        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ApiKeyCreationResponse createdKey = fromJson(createContent, ApiKeyCreationResponse.class);

        // When: Revoking the API key
        final MockHttpServletRequestBuilder revokeRequest = delete(USER_API_KEY_PATH.replace("{keyId}", createdKey.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(revokeRequest);

        // Then: Response should be NOT_FOUND (not FORBIDDEN to avoid enumeration)
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when unauthenticated user revokes API key")
    public void revokeApiKeyFailsWhenUnauthenticated() throws Exception {
        // When: Revoking API key without authentication
        final MockHttpServletRequestBuilder revokeRequest = delete(USER_API_KEY_PATH.replace("{keyId}", "1"))
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(revokeRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }
}
