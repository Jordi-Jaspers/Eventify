package io.github.eventify.api.notification.adapter.controller;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.model.request.TestAdapterConnectionRequest;
import io.github.eventify.api.notification.adapter.model.response.TestConnectionResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static io.github.eventify.api.Paths.ADAPTER_CONFIG_TEST_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Adapter Test Connection Controller")
public class AdapterTestConnectionControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return 200 with success=true when IN_APP adapter sends successfully")
    public void testConnectionSuccessForInAppAdapter() throws Exception {
        // Given: an authenticated user
        final User user = aValidatedUser();
        final TestAdapterConnectionRequest request = new TestAdapterConnectionRequest()
            .setAdapterType(AdapterType.IN_APP)
            .setWebhookUrl("https://example.com/webhook");

        // When: posting to the test endpoint
        final ResultActions response = mockMvc.perform(
            post(ADAPTER_CONFIG_TEST_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: response is 200 OK
        response.andExpect(status().is(SC_OK));

        // And: body has success=true
        final TestConnectionResponse body = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            TestConnectionResponse.class
        );
        assertThat(body.isSuccess(), is(true));
        assertThat(body.getError(), is(nullValue()));
    }

    @Test
    @DisplayName("Should return 200 with success=false when webhook URL is unreachable")
    public void testConnectionWithUnreachableWebhookReturnsSuccessFalse() throws Exception {
        // Given: an authenticated user with a bad Slack webhook URL
        final User user = aValidatedUser();
        final TestAdapterConnectionRequest request = new TestAdapterConnectionRequest()
            .setAdapterType(AdapterType.SLACK)
            .setWebhookUrl("https://hooks.slack.com/services/INVALID/DEAD/BEEF");

        // When: posting to the test endpoint
        final ResultActions response = mockMvc.perform(
            post(ADAPTER_CONFIG_TEST_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: response is still 200 (not a 5xx)
        response.andExpect(status().is(SC_OK));

        // And: body has success=false with an error message
        final TestConnectionResponse body = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            TestConnectionResponse.class
        );
        assertThat(body.isSuccess(), is(false));
        assertThat(body.getError(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return 401 when testing connection without authentication")
    public void testConnectionRequiresAuthentication() throws Exception {
        // Given: no auth header
        final TestAdapterConnectionRequest request = new TestAdapterConnectionRequest()
            .setAdapterType(AdapterType.SLACK)
            .setWebhookUrl("https://example.com/webhook");

        // When: posting without token
        final ResultActions response = mockMvc.perform(
            post(ADAPTER_CONFIG_TEST_PATH)
                .contentType(APPLICATION_JSON)
                .content(toJson(request))
        );

        // Then: unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 400 when adapterType is missing")
    public void testConnectionReturnsBadRequestWhenAdapterTypeMissing() throws Exception {
        // Given: an authenticated user and a request without adapterType
        final User user = aValidatedUser();
        final TestAdapterConnectionRequest request = new TestAdapterConnectionRequest()
            .setWebhookUrl("https://example.com/webhook");

        // When: posting without adapterType
        final ResultActions response = mockMvc.perform(
            post(ADAPTER_CONFIG_TEST_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: bad request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when webhookUrl is missing")
    public void testConnectionReturnsBadRequestWhenWebhookUrlMissing() throws Exception {
        // Given: an authenticated user and a request without webhookUrl
        final User user = aValidatedUser();
        final TestAdapterConnectionRequest request = new TestAdapterConnectionRequest()
            .setAdapterType(AdapterType.SLACK);

        // When: posting without webhookUrl
        final ResultActions response = mockMvc.perform(
            post(ADAPTER_CONFIG_TEST_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: bad request
        response.andExpect(status().is(SC_BAD_REQUEST));
    }
}
