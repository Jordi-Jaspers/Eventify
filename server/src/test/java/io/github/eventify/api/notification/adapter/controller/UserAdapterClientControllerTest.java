package io.github.eventify.api.notification.adapter.controller;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.model.request.CreateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.model.request.UpdateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.model.response.AdapterConfigResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.*;
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

@DisplayName("Integration Test - User Adapter Config Controller")
public class UserAdapterClientControllerTest extends IntegrationTest {

    // ========================= POST /v1/user/adapter-configs =========================

    @Test
    @DisplayName("Should create personal adapter config successfully")
    public void createPersonalAdapterConfigSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: A valid create request (SLACK — non-system-managed type)
        final CreateAdapterConfigRequest request = aValidSlackRequest();

        // When: Creating the config
        final MockHttpServletRequestBuilder createRequest = post(USER_ADAPTER_CONFIGS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain config details
        final AdapterConfigResponse body = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdapterConfigResponse.class
        );
        assertThat(body.getId(), is(notNullValue()));
        assertThat(body.getAdapterType(), is(AdapterType.SLACK));
        assertThat(body.getLabel(), is("My Slack Notifier"));
        assertThat(body.isEnabled(), is(true));
    }

    @Test
    @DisplayName("Should return 400 when creating IN_APP config (system-managed)")
    public void createInAppAdapterConfigIsBlocked() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: IN_APP request (system-managed — not allowed)
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.IN_APP)
            .setLabel("My In-App")
            .setConfig(Map.of());

        // When: Creating
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIGS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: Bad request
        response.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when creating EMAIL config (system-managed)")
    public void createEmailAdapterConfigIsBlocked() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: EMAIL request (system-managed — not allowed)
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.EMAIL)
            .setLabel("My Email")
            .setConfig(Map.of());

        // When: Creating
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIGS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: Bad request
        response.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 when creating config without authentication")
    public void createAdapterConfigUnauthorized() throws Exception {
        // Given: No auth header
        // When: Posting without token
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIGS_PATH)
                .contentType(APPLICATION_JSON)
                .content(toJson(aValidSlackRequest()))
        );

        // Then: Unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 400 when creating config with missing adapterType")
    public void createAdapterConfigValidationFailsMissingType() throws Exception {
        // Given: Authenticated user
        final User user = aValidatedUser();

        // And: Request missing adapterType
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setLabel("My Config")
            .setConfig(Map.of());

        // When: Creating
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIGS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: Bad request
        response.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when SLACK config is missing webhookUrl")
    public void createSlackAdapterConfigFailsMissingWebhookUrl() throws Exception {
        // Given: Authenticated user
        final User user = aValidatedUser();

        // And: SLACK request without webhookUrl
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.SLACK)
            .setLabel("My Slack")
            .setConfig(Map.of());

        // When: Creating
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIGS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: Bad request
        response.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return webhookUrl in response without masking")
    public void createSlackAdapterConfigReturnsWebhookUrl() throws Exception {
        // Given: Authenticated user and valid SLACK request
        final User user = aValidatedUser();
        final String webhookUrl = "https://example.com/webhook/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX";

        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.SLACK)
            .setLabel("Slack Notifier")
            .setConfig(Map.of("webhookUrl", webhookUrl))
            .setEnabled(true);

        // When: Creating the config
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIGS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: Response is CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: webhookUrl in response is returned as-is
        final AdapterConfigResponse body = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdapterConfigResponse.class
        );
        assertThat(body.getConfig().get("webhookUrl"), is(webhookUrl));
    }

    // ========================= GET /v1/user/adapter-configs =========================

    @Test
    @DisplayName("Should list personal adapter configs for current user")
    public void listPersonalAdapterConfigsSuccess() throws Exception {
        // Given: User with two additional adapter configs (plus 2 auto-provisioned on registration)
        final User user = aValidatedUser();
        anAdapterConfigForUser(user, AdapterType.MATTERMOST, "Mattermost 1");
        anAdapterConfigForUser(user, AdapterType.SLACK, "Slack 1");

        // When: Listing personal configs
        final ResultActions response = mockMvc.perform(
            get(USER_ADAPTER_CONFIGS_PATH)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // Then: Response is OK with 4 configs (2 auto-provisioned + 2 manually added)
        response.andExpect(status().is(SC_OK));
        final List<?> body = fromJson(response.andReturn().getResponse().getContentAsString(), List.class);
        assertThat(body, hasSize(4));
    }

    @Test
    @DisplayName("Should not return another user's personal configs")
    public void listPersonalAdapterConfigsOnlyReturnsOwnConfigs() throws Exception {
        // Given: Two users, each with an additional SLACK config (plus 2 auto-provisioned each)
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();
        anAdapterConfigForUser(user1, AdapterType.SLACK, "User1 Slack");
        anAdapterConfigForUser(user2, AdapterType.SLACK, "User2 Slack");

        // When: User1 lists their configs
        final ResultActions response = mockMvc.perform(
            get(USER_ADAPTER_CONFIGS_PATH)
                .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
        );

        // Then: Only user1's configs are returned (2 auto-provisioned + 1 manually added = 3)
        response.andExpect(status().is(SC_OK));
        final List<?> body = fromJson(response.andReturn().getResponse().getContentAsString(), List.class);
        assertThat(body, hasSize(3));
    }

    // ========================= GET /v1/user/adapter-configs/{id} =========================

    @Test
    @DisplayName("Should get personal adapter config by id")
    public void getAdapterConfigSuccess() throws Exception {
        // Given: User's personal SLACK config
        final User user = aValidatedUser();
        final AdapterConfigResponse created = createAndGetConfig(user, aValidSlackRequest());

        // When: Getting by ID
        final ResultActions response = mockMvc.perform(
            get(USER_ADAPTER_CONFIG_PATH, created.getId())
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // Then: Response is OK
        response.andExpect(status().is(SC_OK));
        final AdapterConfigResponse body = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdapterConfigResponse.class
        );
        assertThat(body.getId(), is(created.getId()));
    }

    @Test
    @DisplayName("Should return 403 when accessing another user's config")
    public void getAdapterConfigForbiddenForOtherUser() throws Exception {
        // Given: User1's SLACK config
        final User user1 = aValidatedUser();
        final AdapterConfigResponse created = createAndGetConfig(user1, aValidSlackRequest());

        // And: A different user
        final User user2 = aValidatedUser();

        // When: User2 attempts to access user1's config
        final ResultActions response = mockMvc.perform(
            get(USER_ADAPTER_CONFIG_PATH, created.getId())
                .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue())
        );

        // Then: Forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return 403 for non-existent adapter config (access check fails)")
    public void getAdapterConfigNotFound() throws Exception {
        // Given: Authenticated user and a non-existent ID
        final User user = aValidatedUser();
        final Long nonExistentId = Long.MAX_VALUE;

        // When: Getting non-existent config
        final ResultActions response = mockMvc.perform(
            get(USER_ADAPTER_CONFIG_PATH, nonExistentId)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // Then: Forbidden (security check returns false, Spring returns 403)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= PUT /v1/user/adapter-configs/{id} =========================

    @Test
    @DisplayName("Should update personal adapter config successfully")
    public void updateAdapterConfigSuccess() throws Exception {
        // Given: User's personal SLACK config
        final User user = aValidatedUser();
        final AdapterConfigResponse created = createAndGetConfig(user, aValidSlackRequest());

        // And: An update request
        final UpdateAdapterConfigRequest updateRequest = new UpdateAdapterConfigRequest()
            .setLabel("Updated Label")
            .setConfig(Map.of("webhookUrl", "https://example.com/updated"))
            .setEnabled(false);

        // When: Updating
        final ResultActions response = mockMvc.perform(
            put(USER_ADAPTER_CONFIG_PATH, created.getId())
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(updateRequest))
        );

        // Then: Response is OK with updated label
        response.andExpect(status().is(SC_OK));
        final AdapterConfigResponse body = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            AdapterConfigResponse.class
        );
        assertThat(body.getLabel(), is("Updated Label"));
        assertThat(body.isEnabled(), is(false));
    }

    @Test
    @DisplayName("Should return 403 when updating another user's config")
    public void updateAdapterConfigForbiddenForOtherUser() throws Exception {
        // Given: User1's SLACK config
        final User user1 = aValidatedUser();
        final AdapterConfigResponse created = createAndGetConfig(user1, aValidSlackRequest());

        // And: A different user
        final User user2 = aValidatedUser();

        // When: User2 tries to update user1's config
        final ResultActions response = mockMvc.perform(
            put(USER_ADAPTER_CONFIG_PATH, created.getId())
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue())
                .content(toJson(new UpdateAdapterConfigRequest().setLabel("Hijacked").setConfig(Map.of()).setEnabled(true)))
        );

        // Then: Forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    // ========================= DELETE /v1/user/adapter-configs/{id} =========================

    @Test
    @DisplayName("Should delete personal adapter config successfully")
    public void deleteAdapterConfigSuccess() throws Exception {
        // Given: User's personal SLACK config (non-system-managed)
        final User user = aValidatedUser();
        final AdapterConfigResponse created = createAndGetConfig(user, aValidSlackRequest());

        // When: Deleting
        final ResultActions response = mockMvc.perform(
            delete(USER_ADAPTER_CONFIG_PATH, created.getId())
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // Then: No content
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should return 403 when deleting another user's config")
    public void deleteAdapterConfigForbiddenForOtherUser() throws Exception {
        // Given: User1's SLACK config
        final User user1 = aValidatedUser();
        final AdapterConfigResponse created = createAndGetConfig(user1, aValidSlackRequest());

        // And: A different user
        final User user2 = aValidatedUser();

        // When: User2 tries to delete user1's config
        final ResultActions response = mockMvc.perform(
            delete(USER_ADAPTER_CONFIG_PATH, created.getId())
                .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue())
        );

        // Then: Forbidden
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return 400 when deleting a system-managed config")
    public void deleteSystemManagedConfigIsBlocked() throws Exception {
        // Given: User whose auto-provisioned IN_APP config is system-managed
        final User user = aValidatedUser();
        final io.github.eventify.api.notification.adapter.model.AdapterConfig inAppConfig =
            adapterConfigRepository.findByUserIdAndOrganizationIdIsNull(user.getId())
                .stream()
                .filter(c -> c.getAdapterType() == AdapterType.IN_APP)
                .findFirst()
                .orElseThrow();

        // When: Attempting to delete the system-managed config
        final ResultActions response = mockMvc.perform(
            delete(USER_ADAPTER_CONFIG_PATH, inAppConfig.getId())
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // Then: Bad request (system-managed protection)
        response.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should allow multiple configs of the same adapter type for the same user")
    public void createMultipleConfigsOfSameAdapterType() throws Exception {
        // Given: An authenticated user (auto-provisioned with IN_APP + EMAIL = 2 configs)
        final User user = aValidatedUser();

        // When: Creating two SLACK configs
        mockMvc.perform(
            post(USER_ADAPTER_CONFIGS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(
                    toJson(
                        new CreateAdapterConfigRequest()
                            .setAdapterType(AdapterType.SLACK)
                            .setLabel("First Slack")
                            .setConfig(Map.of("webhookUrl", "https://example.com/webhook/1"))
                            .setEnabled(true)
                    )
                )
        ).andExpect(status().is(SC_CREATED));

        mockMvc.perform(
            post(USER_ADAPTER_CONFIGS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(
                    toJson(
                        new CreateAdapterConfigRequest()
                            .setAdapterType(AdapterType.SLACK)
                            .setLabel("Second Slack")
                            .setConfig(Map.of("webhookUrl", "https://example.com/webhook/2"))
                            .setEnabled(true)
                    )
                )
        ).andExpect(status().is(SC_CREATED));

        // Then: All configs exist (2 auto-provisioned + 2 SLACK)
        final List<?> body = fromJson(
            mockMvc.perform(
                get(USER_ADAPTER_CONFIGS_PATH)
                    .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            ).andReturn().getResponse().getContentAsString(),
            List.class
        );
        assertThat(body, hasSize(4));
    }

    // ========================= FACTORY METHODS =========================

    private static CreateAdapterConfigRequest aValidSlackRequest() {
        return new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.SLACK)
            .setLabel("My Slack Notifier")
            .setConfig(Map.of("webhookUrl", "https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXX"))
            .setEnabled(true);
    }

    private AdapterConfigResponse createAndGetConfig(
        final User user,
        final CreateAdapterConfigRequest request
    ) throws Exception {
        final ResultActions response = mockMvc.perform(
            post(USER_ADAPTER_CONFIGS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );
        return fromJson(response.andReturn().getResponse().getContentAsString(), AdapterConfigResponse.class);
    }
}
