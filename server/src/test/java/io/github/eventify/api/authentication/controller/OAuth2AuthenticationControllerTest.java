package io.github.eventify.api.authentication.controller;

import io.github.eventify.api.user.model.User;
import io.github.eventify.common.security.oauth2.CustomOAuth2UserService;
import io.github.eventify.common.security.oauth2.OAuth2AuthenticationSuccessHandler;
import io.github.eventify.support.IntegrationTest;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.USER_DETAILS;
import static io.github.eventify.common.constant.Constants.OAuthAttributes.EMAIL;
import static io.github.eventify.common.constant.Constants.Security.ACCESS_TOKEN_COOKIE;
import static io.github.eventify.common.constant.Constants.Security.REFRESH_TOKEN_COOKIE;
import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.GITHUB_REGISTRATION_ID;
import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.GOOGLE_REGISTRATION_ID;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the complete OAuth2 login flow using MockOAuth2Server. These tests call the real CustomOAuth2UserService to verify
 * the actual implementation, then test cookie-based access to protected resources (simulating frontend behavior).
 */
@DisplayName("Integration Test - OAuth2 Complete Login Flow")
public class OAuth2AuthenticationControllerTest extends IntegrationTest {

    @MockitoSpyBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoSpyBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Test
    @DisplayName("Should complete full OAuth2 login flow with Google and access protected resource with cookies")
    public void shouldCompleteFullOAuth2LoginFlowWithGoogleAndAccessProtectedResourceWithCookies() throws Exception {
        // Given: A new Google OAuth2 user with verified email (Google uses explicit email_verified attribute)
        final OAuth2User googleUser = aGoogleOAuth2User(true);

        // And: A mock OAuth2UserRequest for Google
        final OAuth2UserRequest userRequest = anOAuthRequestVia(GOOGLE_REGISTRATION_ID);

        // When: OAuth2 authentication occurs (calling the REAL CustomOAuth2UserService)
        customOAuth2UserService.processOAuth2User(userRequest, googleUser);

        // Then: The user should be created in the database (testing REAL CustomOAuth2UserService logic)
        final String email = (String) googleUser.getAttributes().get(EMAIL);
        final User createdUser = tokenService.generateAuthorizationTokens(getUserDetails(email), null);
        assertThat(createdUser, is(notNullValue()));
        assertThat(createdUser.getFirstName(), is(notNullValue()));
        assertThat(createdUser.getLastName(), is(notNullValue()));
        assertThat(createdUser.isEnabled(), is(true));
        assertThat(createdUser.isValidated(), is(true));

        // When: Accessing a protected resource with the generated tokens as cookies (simulating frontend)
        final MockHttpServletRequestBuilder protectedResourceRequest = get(USER_DETAILS)
            .cookie(new Cookie(ACCESS_TOKEN_COOKIE, createdUser.getAccessToken().getValue()))
            .cookie(new Cookie(REFRESH_TOKEN_COOKIE, createdUser.getRefreshToken().getValue()))
            .contentType(APPLICATION_JSON);

        final ResultActions protectedResourceResponse = mockMvc.perform(protectedResourceRequest);

        // Then: The protected resource should be accessible
        protectedResourceResponse.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should complete full OAuth2 login flow with GitHub and access protected resource with cookies")
    public void shouldCompleteFullOAuth2LoginFlowWithGitHubAndAccessProtectedResourceWithCookies() throws Exception {
        // Given: A new GitHub OAuth2 user with verified email
        final OAuth2User githubUser = aGithubOAuthUser(true);

        // And: A mock OAuth2UserRequest for GitHub
        final OAuth2UserRequest userRequest = anOAuthRequestVia(GITHUB_REGISTRATION_ID);

        // When: OAuth2 authentication occurs (calling the REAL CustomOAuth2UserService)
        customOAuth2UserService.processOAuth2User(userRequest, githubUser);

        // Then: The user should be created in the database (testing REAL CustomOAuth2UserService logic)
        final String email = (String) githubUser.getAttributes().get(EMAIL);
        final User createdUser = tokenService.generateAuthorizationTokens(getUserDetails(email), null);
        assertThat(createdUser, is(notNullValue()));
        assertThat(createdUser.getFirstName(), is(notNullValue()));
        assertThat(createdUser.getLastName(), is(notNullValue()));
        assertThat(createdUser.isEnabled(), is(true));
        assertThat(createdUser.isValidated(), is(true));

        // When: Accessing a protected resource with the generated tokens as cookies (simulating frontend)
        final MockHttpServletRequestBuilder protectedResourceRequest = get(USER_DETAILS)
            .cookie(new Cookie(ACCESS_TOKEN_COOKIE, createdUser.getAccessToken().getValue()))
            .cookie(new Cookie(REFRESH_TOKEN_COOKIE, createdUser.getRefreshToken().getValue()))
            .contentType(APPLICATION_JSON);

        final ResultActions protectedResourceResponse = mockMvc.perform(protectedResourceRequest);

        // Then: The protected resource should be accessible
        protectedResourceResponse.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should not create duplicate users when logging in via OAuth with an existing account")
    public void shouldNotCreateDuplicateUsersWhenLoggingInViaOAuthWithExistingAccount() {
        // Given: An existing user in the database
        final User existingUser = aValidatedUser();

        // And: A new GitHub OAuth2 user with verified email
        final OAuth2User githubUser = aGithubOAuthUser(existingUser.getEmail(), true);

        // And: A mock OAuth2UserRequest for GitHub
        final OAuth2UserRequest userRequest = anOAuthRequestVia(GITHUB_REGISTRATION_ID);

        // When: OAuth2 authentication occurs (calling the REAL CustomOAuth2UserService)
        customOAuth2UserService.processOAuth2User(userRequest, githubUser);

        // Then: The user should exist and no error was thrown
        final String email = (String) githubUser.getAttributes().get(EMAIL);
        final User createdUser = getUserDetails(email);
        assertThat(createdUser, is(notNullValue()));
    }
}
