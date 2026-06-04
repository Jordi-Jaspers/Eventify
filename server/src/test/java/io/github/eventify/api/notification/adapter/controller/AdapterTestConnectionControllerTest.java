package io.github.eventify.api.notification.adapter.controller;

import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.model.response.TestConnectionResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static io.github.eventify.api.Paths.USER_ADAPTER_CONFIG_TEST_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Adapter Test Connection Controller")
public class AdapterTestConnectionControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return 200 with success=true when connection test passes")
    public void testConnectionSuccessReturnsOkWithSuccessTrue() throws Exception {
        // Given: an authenticated user with a valid adapter config
        final User user = aValidatedUser();
        final Long configId = anAdapterConfigForUser(user, AdapterType.IN_APP, "In-App Test").getId();

        // When: posting to the test endpoint
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIG_TEST_PATH, configId)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
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
        // Given: an authenticated user with a Slack config pointing to a bad URL
        final User user = aValidatedUser();
        final Long configId = anAdapterConfigForUserWithConfig(
            user,
            AdapterType.SLACK,
            "Broken Slack",
            Map.of("webhookUrl", "https://hooks.slack.com/services/INVALID/DEAD/BEEF")
        ).getId();

        // When: posting to the test endpoint
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIG_TEST_PATH, configId)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
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
        // Given: no auth header and any config ID
        // When: posting without token
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIG_TEST_PATH, 1L)
        );

        // Then: unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 403 when testing another user's config")
    public void testConnectionForbiddenForOtherUsersConfig() throws Exception {
        // Given: user1 owns a config
        final User user1 = aValidatedUser();
        final Long configId = anAdapterConfigForUser(user1, AdapterType.IN_APP, "User1 Config").getId();

        // And: user2 tries to access it
        final User user2 = aValidatedUser();

        // When: user2 posts to test user1's config
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIG_TEST_PATH, configId)
                .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue())
        );

        // Then: forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return 403 for non-existent config ID")
    public void testConnectionForbiddenForNonExistentConfig() throws Exception {
        // Given: authenticated user and a non-existent config ID
        final User user = aValidatedUser();

        // When: testing a non-existent config
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIG_TEST_PATH, Long.MAX_VALUE)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // Then: forbidden (security check fails for non-owned/non-existent resource)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= FACTORY METHODS =========================

    private AdapterConfig anAdapterConfigForUserWithConfig(
        final User user,
        final AdapterType adapterType,
        final String label,
        final Map<String, Object> configMap
    ) {
        final AdapterConfig config = new AdapterConfig();
        config.setUser(user);
        config.setAdapterType(adapterType);
        config.setLabel(label);
        config.setConfig(configMap);
        config.setEnabled(true);
        return adapterConfigRepository.save(config);
    }
}
