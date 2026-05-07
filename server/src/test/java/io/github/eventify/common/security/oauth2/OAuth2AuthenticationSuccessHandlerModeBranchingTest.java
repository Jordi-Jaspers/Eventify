package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.authentication.service.CookieService;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.support.UnitTest;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;

import static io.github.eventify.common.constant.Constants.OAuthAttributes.EMAIL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OAuth2AuthenticationSuccessHandler} — mode branching (login vs link).
 */
@DisplayName("Unit Test - OAuth2AuthenticationSuccessHandler - Mode Branching")
public class OAuth2AuthenticationSuccessHandlerModeBranchingTest extends UnitTest {

    private static final String USER_EMAIL = "user@example.com";
    private static final String SECURITY_PATH = "/profile/security";

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    @Mock
    private CookieService cookieService;

    @Mock
    private OAuth2RedirectHelper redirectHelper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private RedirectStrategy redirectStrategy;

    private OAuth2AuthenticationSuccessHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new OAuth2AuthenticationSuccessHandler(
            tokenService,
            userService,
            cookieService,
            redirectHelper
        );
        handler.setRedirectStrategy(redirectStrategy);
    }

    @AfterEach
    public void tearDown() {
        OAuth2AttributesHolder.clear();
    }

    @Test
    @DisplayName("Login mode: Should issue JWT and redirect to dashboard (default behavior preserved)")
    public void onAuthenticationSuccess_loginMode_issuesJwtAndRedirectsToDashboard() throws IOException {
        // Given: A response that is not committed
        when(response.isCommitted()).thenReturn(false);

        // And: OAuth2 user with email attribute
        when(oAuth2User.getAttribute(EMAIL)).thenReturn(USER_EMAIL);

        // And: resolvedUserId stashed by CustomOAuth2UserService
        OAuth2AttributesHolder.setAttribute("resolvedUserId", 42L);

        // And: Authentication with mode=login (no mode param = login)
        final Authentication authentication = buildOAuth2AuthenticationWithMode("google", null);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        // And: User service returns a valid user by id
        final User user = aValidUserWithTokens();
        when(userService.findById(42L)).thenReturn(user);
        when(userService.updateUserDetails(any(User.class))).thenReturn(user);
        when(tokenService.generateAuthorizationTokens(any(User.class), any(), anyBoolean())).thenReturn(user);

        final String dashboardUrl = APPLICATION_URL + "/dashboard";
        when(redirectHelper.buildRedirectUrl()).thenReturn(dashboardUrl);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: JWT tokens are issued
        verify(tokenService, times(1)).generateAuthorizationTokens(any(User.class), any(), anyBoolean());
        verify(cookieService, times(1)).setAuthCookies(any(), any(), any());

        // And: findById was used (not loadUserByUsername)
        verify(userService, times(1)).findById(42L);
        verify(userService, never()).loadUserByUsername(any());

        // And: Redirect goes to dashboard (not security)
        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy, times(1)).sendRedirect(any(), any(), urlCaptor.capture());
        assertThat(urlCaptor.getValue(), not(containsString("security")));
    }

    @Test
    @DisplayName("Login mode fallback: Should use loadUserByUsername when resolvedUserId is absent")
    public void onAuthenticationSuccess_loginMode_fallsBackToLoadUserByUsername_whenResolvedUserIdAbsent() throws IOException {
        // Given: A response that is not committed
        when(response.isCommitted()).thenReturn(false);

        // And: OAuth2 user with email attribute
        when(oAuth2User.getAttribute(EMAIL)).thenReturn(USER_EMAIL);

        // And: No resolvedUserId in holder (simulates edge case)
        // (holder is cleared in @AfterEach, nothing set here)

        // And: Authentication with mode=login
        final Authentication authentication = buildOAuth2AuthenticationWithMode("google", null);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        // And: User service returns a valid user by email
        final User user = aValidUserWithTokens();
        when(userService.loadUserByUsername(USER_EMAIL)).thenReturn(user);
        when(userService.updateUserDetails(any(User.class))).thenReturn(user);
        when(tokenService.generateAuthorizationTokens(any(User.class), any(), anyBoolean())).thenReturn(user);

        final String dashboardUrl = APPLICATION_URL + "/dashboard";
        when(redirectHelper.buildRedirectUrl()).thenReturn(dashboardUrl);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: loadUserByUsername was used as fallback
        verify(userService, times(1)).loadUserByUsername(USER_EMAIL);
        verify(userService, never()).findById(any());

        // And: JWT tokens are issued
        verify(tokenService, times(1)).generateAuthorizationTokens(any(User.class), any(), anyBoolean());
    }

    @Test
    @DisplayName("Link mode: Should NOT issue JWT and redirect to /profile/security?linked={provider}")
    public void onAuthenticationSuccess_linkMode_doesNotIssueJwtAndRedirectsToConnectedAccountsWithLinkedParam() throws IOException {
        // Given: A response that is not committed
        when(response.isCommitted()).thenReturn(false);

        // And: OAuth2 user with email attribute
        when(oAuth2User.getAttribute(EMAIL)).thenReturn(USER_EMAIL);

        // And: Authentication with mode=link in additionalParameters, registrationId=google
        final Authentication authentication = buildOAuth2AuthenticationWithMode("google", "link");
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        final String connectedAccountsUrl = APPLICATION_URL + SECURITY_PATH + "?linked=google";
        when(redirectHelper.buildLinkSuccessRedirectUrl("google")).thenReturn(connectedAccountsUrl);

        // When: Handling authentication success in link mode
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: JWT tokens are NOT issued (session preserved)
        verify(tokenService, never()).generateAuthorizationTokens(any(), any(), anyBoolean());
        verify(cookieService, never()).setAuthCookies(any(), any(), any());

        // And: Redirect goes to /profile/security?linked=google
        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy, times(1)).sendRedirect(any(), any(), urlCaptor.capture());
        assertThat(urlCaptor.getValue(), containsString("security"));
        assertThat(urlCaptor.getValue(), containsString("linked=google"));
    }

    @Test
    @DisplayName("Link mode: Should redirect with correct provider name in linked param")
    public void onAuthenticationSuccess_linkMode_includesCorrectProviderInRedirect() throws IOException {
        // Given: A response that is not committed
        when(response.isCommitted()).thenReturn(false);

        // And: OAuth2 user with email attribute
        when(oAuth2User.getAttribute(EMAIL)).thenReturn(USER_EMAIL);

        // And: Authentication with mode=link via GitHub
        final Authentication authentication = buildOAuth2AuthenticationWithMode("github", "link");
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        final String connectedAccountsUrl = APPLICATION_URL + SECURITY_PATH + "?linked=github";
        when(redirectHelper.buildLinkSuccessRedirectUrl("github")).thenReturn(connectedAccountsUrl);

        // When: Handling authentication success in link mode
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Redirect contains linked=github
        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy, times(1)).sendRedirect(any(), any(), urlCaptor.capture());
        assertThat(urlCaptor.getValue(), containsString("linked=github"));
    }

    // ========================= Helper methods =========================

    private Authentication buildOAuth2AuthenticationWithMode(final String registrationId, final String mode) {
        final Authentication authentication = mock(OAuth2AuthenticationToken.class);
        when(((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId()).thenReturn(registrationId);
        if (mode != null) {
            OAuth2AttributesHolder.setAttributes(Map.of("mode", mode));
        }
        return authentication;
    }
}
