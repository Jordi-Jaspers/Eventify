package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.authentication.service.CookieService;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.support.UnitTest;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;

import static io.github.eventify.api.Paths.OAUTH2_FRONTEND_REDIRECT_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OAuth2AuthenticationSuccessHandler} - buildRedirectUrl method.
 */
@DisplayName("Unit Test - OAuth2AuthenticationSuccessHandler - Build Redirect URL")
public class OAuth2AuthenticationSuccessHandlerBuildRedirectUrlTest extends UnitTest {

    private static final String ALTERNATIVE_URL = "https://example.com";

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
    private Authentication authentication;

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
        when(cookieService.readDeviceId(any(HttpServletRequest.class))).thenReturn(Optional.of(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Should build correct redirect URL with application URL")
    public void shouldBuildCorrectRedirectUrlWithApplicationUrl() throws IOException {
        // Given: A redirect URL is configured in helper
        when(redirectHelper.buildRedirectUrl()).thenReturn(APPLICATION_URL + OAUTH2_FRONTEND_REDIRECT_PATH);

        // And: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: A valid user in the system
        final User user = aValidUser();
        when(userService.loadUserByUsername(VALID_EMAIL)).thenReturn(user);

        // And: Tokens are generated for the user
        final User userWithTokens = aValidUserWithTokens();
        when(tokenService.generateAuthorizationTokens(any(User.class), any(HttpServletRequest.class), eq(false), any(UUID.class)))
            .thenReturn(
                userWithTokens
            );

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Redirect should occur to the correct URL
        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy, times(1)).sendRedirect(
            eq(request),
            eq(response),
            urlCaptor.capture()
        );
        final String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl, is(equalTo(APPLICATION_URL + OAUTH2_FRONTEND_REDIRECT_PATH)));
    }

    @Test
    @DisplayName("Should build redirect URL with correct path suffix")
    public void shouldBuildRedirectUrlWithCorrectPathSuffix() throws IOException {
        // Given: A redirect URL is configured in helper
        when(redirectHelper.buildRedirectUrl()).thenReturn(APPLICATION_URL + OAUTH2_FRONTEND_REDIRECT_PATH);

        // And: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: A valid user in the system
        final User user = aValidUser();
        when(userService.loadUserByUsername(VALID_EMAIL)).thenReturn(user);

        // And: Tokens are generated for the user
        final User userWithTokens = aValidUserWithTokens();
        when(tokenService.generateAuthorizationTokens(any(User.class), any(HttpServletRequest.class), eq(false), any(UUID.class)))
            .thenReturn(
                userWithTokens
            );

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Redirect URL should contain the OAuth2 redirect path
        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy, times(1)).sendRedirect(
            eq(request),
            eq(response),
            urlCaptor.capture()
        );
        final String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl, containsString(OAUTH2_FRONTEND_REDIRECT_PATH));
    }

    @Test
    @DisplayName("Should build redirect URL with alternative application URL")
    public void shouldBuildRedirectUrlWithAlternativeApplicationUrl() throws IOException {
        // Given: An alternative redirect URL is configured in helper
        when(redirectHelper.buildRedirectUrl()).thenReturn(ALTERNATIVE_URL + OAUTH2_FRONTEND_REDIRECT_PATH);

        // And: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: A valid user in the system
        final User user = aValidUser();
        when(userService.loadUserByUsername(VALID_EMAIL)).thenReturn(user);

        // And: Tokens are generated for the user
        final User userWithTokens = aValidUserWithTokens();
        when(tokenService.generateAuthorizationTokens(any(User.class), any(HttpServletRequest.class), eq(false), any(UUID.class)))
            .thenReturn(
                userWithTokens
            );

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Redirect should occur to the correct URL with alternative base
        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy, times(1)).sendRedirect(
            eq(request),
            eq(response),
            urlCaptor.capture()
        );
        final String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl, is(equalTo(ALTERNATIVE_URL + OAUTH2_FRONTEND_REDIRECT_PATH)));
    }
}
