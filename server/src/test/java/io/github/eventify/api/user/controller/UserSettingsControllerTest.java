package io.github.eventify.api.user.controller;

import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.resource.ApiErrorResponseResource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.USER_RETENTION_SETTINGS_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - User Settings Controller")
public class UserSettingsControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return default retention days for authenticated user")
    public void getRetentionSettingsSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Requesting retention settings
        final MockHttpServletRequestBuilder request = get(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain default retention days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(90));
    }

    @Test
    @DisplayName("Should update retention days successfully with valid value")
    public void updateRetentionSettingsSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Valid retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(365);

        // When: Updating retention settings
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain updated retention days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(365));
    }

    @Test
    @DisplayName("Should update retention days to 90 days")
    public void updateRetentionTo90Days() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Retention request for 90 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(90);

        // When: Updating retention settings
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Retention should be 90 days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(90));
    }

    @Test
    @DisplayName("Should update retention days to 180 days")
    public void updateRetentionTo180Days() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Retention request for 180 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(180);

        // When: Updating retention settings
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Retention should be 180 days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(180));
    }

    @Test
    @DisplayName("Should update retention days to 730 days")
    public void updateRetentionTo730Days() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Retention request for 730 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(730);

        // When: Updating retention settings
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Retention should be 730 days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(730));
    }

    @Test
    @DisplayName("Should update retention days to 1095 days")
    public void updateRetentionTo1095Days() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Retention request for 1095 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(1095);

        // When: Updating retention settings
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Retention should be 1095 days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(1095));
    }

    @Test
    @DisplayName("Should update retention days to 1825 days")
    public void updateRetentionTo1825Days() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Retention request for 1825 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(1825);

        // When: Updating retention settings
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Retention should be 1825 days
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(1825));
    }

    @Test
    @DisplayName("Should return bad request when retention days is invalid value 100")
    public void updateRetentionInvalidValue100() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Invalid retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(100);

        // When: Updating retention settings with invalid value
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain validation error
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);

        assertThat(error.getApiErrorReason(), containsString("retention"));
    }

    @Test
    @DisplayName("Should return bad request when retention days is invalid value 500")
    public void updateRetentionInvalidValue500() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Invalid retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(500);

        // When: Updating retention settings with invalid value
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return bad request when retention days is negative")
    public void updateRetentionNegativeValue() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Negative retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(-1);

        // When: Updating retention settings with negative value
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return bad request when retention days is zero")
    public void updateRetentionZeroValue() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Zero retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(0);

        // When: Updating retention settings with zero value
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return bad request when retention days is null")
    public void updateRetentionNullValue() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Null retention request
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(null);

        // When: Updating retention settings with null value
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return unauthorized when not authenticated")
    public void getRetentionSettingsUnauthorized() throws Exception {
        // Given: No authentication

        // When: Requesting retention settings without auth
        final MockHttpServletRequestBuilder request = get(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return unauthorized when updating without authentication")
    public void updateRetentionSettingsUnauthorized() throws Exception {
        // Given: Valid retention request but no authentication
        final UpdateRetentionRequest request = new UpdateRetentionRequest()
            .setRetentionDays(365);

        // When: Updating retention settings without auth
        final MockHttpServletRequestBuilder updateRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return updated retention when user checks after update")
    public void getRetentionAfterUpdate() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has updated retention to 730 days
        final UpdateRetentionRequest updateRequest = new UpdateRetentionRequest()
            .setRetentionDays(730);

        final MockHttpServletRequestBuilder updateHttpRequest = put(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(updateRequest));

        mockMvc.perform(updateHttpRequest);

        // When: Requesting retention settings
        final MockHttpServletRequestBuilder getRequest = get(USER_RETENTION_SETTINGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should show updated retention
        final String content = response.andReturn().getResponse().getContentAsString();
        final RetentionSettingsResponse retentionSettings = fromJson(content, RetentionSettingsResponse.class);

        assertThat(retentionSettings.getRetentionDays(), is(730));
    }

    /**
     * Response DTO for retention settings (to be created in implementation).
     */
    private static class RetentionSettingsResponse {

        private Integer retentionDays;

        public Integer getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(final Integer retentionDays) {
            this.retentionDays = retentionDays;
        }
    }


    /**
     * Request DTO for updating retention settings (to be created in implementation).
     */
    private static class UpdateRetentionRequest {

        private Integer retentionDays;

        public Integer getRetentionDays() {
            return retentionDays;
        }

        public UpdateRetentionRequest setRetentionDays(final Integer retentionDays) {
            this.retentionDays = retentionDays;
            return this;
        }
    }
}
