package io.github.eventify.api.quota.controller;

import io.github.eventify.api.quota.model.response.UserQuotaResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.USER_QUOTA_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - User Quota Controller")
public class UserQuotaControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return quota status for authenticated user")
    public void getQuotaSuccess() throws Exception {
        // Given: Authenticated user
        final User user = aValidatedUser();

        // When: Requesting quota status
        final MockHttpServletRequestBuilder request = get(USER_QUOTA_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON);

        final ResultActions result = mockMvc.perform(request);

        // Then: Response should be OK
        result.andExpect(status().is(SC_OK));

        // And: Response should contain quota data
        final String content = result.andReturn().getResponse().getContentAsString();
        final UserQuotaResponse quotaResponse = fromJson(content, UserQuotaResponse.class);

        assertThat(quotaResponse.getUsed(), is(notNullValue()));
        assertThat(quotaResponse.getLimit(), is(1000));
        assertThat(quotaResponse.getRemaining(), is(notNullValue()));
        assertThat(quotaResponse.getPeriodStart(), is(notNullValue()));
        assertThat(quotaResponse.getPeriodEnd(), is(notNullValue()));
        assertThat(quotaResponse.getPercentUsed(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return unauthorized when not authenticated")
    public void getQuotaUnauthorized() throws Exception {
        // Given: No authentication

        // When: Requesting quota status without token
        final MockHttpServletRequestBuilder request = get(USER_QUOTA_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions result = mockMvc.perform(request);

        // Then: Response should be unauthorized
        result.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return correct format with all required fields")
    public void getQuotaReturnsCorrectFormat() throws Exception {
        // Given: Authenticated user
        final User user = aValidatedUser();

        // When: Requesting quota status
        final MockHttpServletRequestBuilder request = get(USER_QUOTA_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON);

        final ResultActions result = mockMvc.perform(request);

        // Then: Response should be OK
        result.andExpect(status().is(SC_OK));

        // And: Response should have all required fields
        final String content = result.andReturn().getResponse().getContentAsString();
        final UserQuotaResponse quotaResponse = fromJson(content, UserQuotaResponse.class);

        assertThat(quotaResponse.getUsed(), is(greaterThanOrEqualTo(0)));
        assertThat(quotaResponse.getLimit(), is(1000));
        assertThat(quotaResponse.getRemaining(), is(lessThanOrEqualTo(1000)));
        assertThat(quotaResponse.getPeriodStart(), is(notNullValue()));
        assertThat(quotaResponse.getPeriodEnd(), is(notNullValue()));
        assertThat(quotaResponse.getPercentUsed(), is(greaterThanOrEqualTo(0.0)));
        assertThat(quotaResponse.getPercentUsed(), is(lessThanOrEqualTo(100.0)));
    }

    @Test
    @DisplayName("Should return zero used for new user")
    public void getQuotaForNewUserReturnsZeroUsed() throws Exception {
        // Given: New authenticated user with no events
        final User user = aValidatedUser();

        // When: Requesting quota status for first time
        final MockHttpServletRequestBuilder request = get(USER_QUOTA_PATH)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON);

        final ResultActions result = mockMvc.perform(request);

        // Then: Response should be OK
        result.andExpect(status().is(SC_OK));

        // And: Used should be 0, remaining should be 1000
        final String content = result.andReturn().getResponse().getContentAsString();
        final UserQuotaResponse quotaResponse = fromJson(content, UserQuotaResponse.class);

        assertThat(quotaResponse.getUsed(), is(0));
        assertThat(quotaResponse.getRemaining(), is(1000));
        assertThat(quotaResponse.getPercentUsed(), is(0.0));
    }
}
