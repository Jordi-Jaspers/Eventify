package io.github.eventify.api.user.controller;

import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.response.ProviderResponse;
import io.github.eventify.support.IntegrationTest;
import io.github.eventify.support.TestBuilders;
import tools.jackson.core.type.TypeReference;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.USER_PROVIDERS_PATH;
import static io.github.eventify.api.Paths.USER_PROVIDER_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - User Provider Controller")
public class UserProviderControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return all provider states when authenticated")
    public void getProviders_returnsAllProviderStates_whenAuthenticated() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // When: Requesting the list of providers
        final MockHttpServletRequestBuilder request = get(USER_PROVIDERS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The response should contain provider entries
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<ProviderResponse> providers = fromJson(content, new TypeReference<>() {});


        // And: LOCAL provider should be connected (user registered with password)
        assertThat(providers, is(not(empty())));
        final boolean hasLocalConnected = providers.stream()
            .anyMatch(p -> "LOCAL".equals(p.getProvider().name()) && p.isConnected());
        assertThat(hasLocalConnected, is(true));
    }

    @Test
    @DisplayName("Should return 401 when listing providers without authentication")
    public void getProviders_returns401_whenUnauthenticated() throws Exception {
        // When: Requesting providers without auth
        final MockHttpServletRequestBuilder request = get(USER_PROVIDERS_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 204 on successful provider unlink")
    public void deleteProvider_returns204_onSuccessfulUnlink() throws Exception {
        // Given: A validated user with a linked OAuth2 provider
        final User user = aValidatedUser();
        final Long googleProviderId = userAuthProviderRepository.save(TestBuilders.aGoogleUserAuthProvider(user)).getId();

        // When: Unlinking the GOOGLE provider
        final MockHttpServletRequestBuilder request = delete(USER_PROVIDER_PATH, googleProviderId)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should return 401 when deleting provider without authentication")
    public void deleteProvider_returns401_whenUnauthenticated() throws Exception {
        // When: Deleting a provider without auth
        final MockHttpServletRequestBuilder request = delete(USER_PROVIDER_PATH, 1L);

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 404 when provider is not found")
    public void deleteProvider_returns404_whenProviderNotFound() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // When: Deleting a non-existent provider
        final MockHttpServletRequestBuilder request = delete(USER_PROVIDER_PATH, 999999L)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should return 404 when provider is owned by a different user (no existence leak)")
    public void deleteProvider_returns404_whenProviderOwnedByDifferentUser() throws Exception {
        // Given: Two validated users
        final User victim = aValidatedUser();
        final User attacker = aValidatedUser();

        // And: Victim has a linked GOOGLE provider
        final Long victimProviderId = userAuthProviderRepository.save(TestBuilders.aGoogleUserAuthProvider(victim)).getId();

        // When: Attacker tries to delete victim's provider
        final MockHttpServletRequestBuilder request = delete(USER_PROVIDER_PATH, victimProviderId)
            .header(AUTHORIZATION, BEARER + attacker.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be NOT_FOUND (no existence leak)
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should return 409 when unlinking last auth method")
    public void deleteProvider_returnsError_whenUnlinkingLastAuthMethod() throws Exception {
        // Given: A validated user whose only auth method is LOCAL (registered via password)
        final User user = aValidatedUser();

        // And: Get the LOCAL provider ID
        final Long localProviderId = userAuthProviderRepository.findByUserAndProvider(user, AuthProvider.LOCAL).orElseThrow().getId();

        // When: Trying to unlink the only LOCAL provider
        final MockHttpServletRequestBuilder request = delete(USER_PROVIDER_PATH, localProviderId)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should indicate a conflict
        response.andExpect(status().is(SC_CONFLICT));
    }

    @Test
    @DisplayName("U4: Should return 409 when attempting to unlink LOCAL provider")
    public void deleteProvider_U4_returns409_whenProviderIsLocal() throws Exception {
        // Given: A validated user with LOCAL provider (and also a GOOGLE provider so last-method check passes)
        final User user = aValidatedUser();
        userAuthProviderRepository.save(TestBuilders.aGoogleUserAuthProvider(user));

        // And: Get the LOCAL provider ID
        final Long localProviderId = userAuthProviderRepository.findByUserAndProvider(user, AuthProvider.LOCAL).orElseThrow().getId();

        // When: Attempting to unlink the LOCAL provider
        final MockHttpServletRequestBuilder request = delete(USER_PROVIDER_PATH, localProviderId)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be 409 Conflict (LOCAL_PROVIDER_UNLINK_ERROR)
        response.andExpect(status().is(SC_CONFLICT));
    }
}
