package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.authentication.service.CookieService;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.support.UnitTest;

import java.io.IOException;
import java.time.OffsetDateTime;
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
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OAuth2AuthenticationSuccessHandler} - authorize method.
 */
@DisplayName("Unit Test - OAuth2AuthenticationSuccessHandler - Authorize")
public class OAuth2AuthenticationSuccessHandlerAuthorizeTest extends UnitTest {

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

    private UUID deviceId;

    @BeforeEach
    public void setUp() {
        handler = new OAuth2AuthenticationSuccessHandler(
            tokenService,
            userService,
            cookieService,
            redirectHelper
        );
        handler.setRedirectStrategy(redirectStrategy);
        when(redirectHelper.buildRedirectUrl()).thenReturn(APPLICATION_URL + OAUTH2_FRONTEND_REDIRECT_PATH);

        // Default: device cookie is present
        deviceId = UUID.randomUUID();
        when(cookieService.readDeviceId(any(HttpServletRequest.class))).thenReturn(Optional.of(deviceId));
    }

    @Test
    @DisplayName("Should authorize user successfully with valid email")
    public void shouldAuthorizeUserSuccessfullyWithValidEmail() throws IOException {
        // Given: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: A valid user in the system
        final User user = aValidUser();
        when(userService.loadUserByUsername(VALID_EMAIL)).thenReturn(user);

        // And: Tokens are generated for the user (OAuth2 always uses rememberMe=false, passes familyId)
        final User userWithTokens = aValidUserWithTokens();
        when(tokenService.generateAuthorizationTokens(any(User.class), any(HttpServletRequest.class), eq(false), eq(deviceId))).thenReturn(
            userWithTokens
        );

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: The user should be loaded by username
        verify(userService, times(1)).loadUserByUsername(VALID_EMAIL);

        // And: The user's last login should be updated
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).updateUserDetails(userCaptor.capture());
        final User updatedUser = userCaptor.getValue();
        assertThat(updatedUser.getLastLogin(), is(notNullValue()));

        // And: Tokens should be generated with rememberMe=false and the device familyId
        verify(tokenService, times(1)).generateAuthorizationTokens(any(User.class), any(HttpServletRequest.class), eq(false), eq(deviceId));

        // And: Cookies should be set with correct token values
        verify(cookieService, times(1)).setAuthCookies(
            eq(response),
            eq(userWithTokens.getAccessToken()),
            eq(userWithTokens.getRefreshToken())
        );

        // And: Redirect should occur to the correct URL
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
    @DisplayName("Should update user last login to current time")
    public void shouldUpdateUserLastLoginToCurrentTime() throws IOException {
        // Given: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: A valid user without last login
        final User user = aValidUser();
        user.setLastLogin(null);
        when(userService.loadUserByUsername(VALID_EMAIL)).thenReturn(user);

        // And: Tokens are generated for the user (OAuth2 always uses rememberMe=false, passes familyId)
        final User userWithTokens = aValidUserWithTokens();
        when(tokenService.generateAuthorizationTokens(any(User.class), any(HttpServletRequest.class), eq(false), eq(deviceId))).thenReturn(
            userWithTokens
        );

        // And: Capture the current time
        final OffsetDateTime beforeAuth = OffsetDateTime.now(UTC);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // And: Capture the time after authentication
        final OffsetDateTime afterAuth = OffsetDateTime.now(UTC);

        // Then: The user's last login should be updated
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).updateUserDetails(userCaptor.capture());
        final User updatedUser = userCaptor.getValue();

        // And: The last login should be between before and after times
        assertThat(updatedUser.getLastLogin(), is(notNullValue()));
        final boolean isAfterOrEqualToBefore = updatedUser.getLastLogin().isAfter(beforeAuth) || updatedUser.getLastLogin().isEqual(
            beforeAuth
        );
        final boolean isBeforeOrEqualToAfter = updatedUser.getLastLogin().isBefore(afterAuth) || updatedUser.getLastLogin().isEqual(
            afterAuth
        );
        assertThat(isAfterOrEqualToBefore, is(true));
        assertThat(isBeforeOrEqualToAfter, is(true));
    }

    @Test
    @DisplayName("Should generate tokens for user")
    public void shouldGenerateTokensForUser() throws IOException {
        // Given: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: A valid user in the system
        final User user = aValidUser();
        when(userService.loadUserByUsername(VALID_EMAIL)).thenReturn(user);

        // And: Tokens are generated for the user (OAuth2 always uses rememberMe=false, passes familyId)
        final User userWithTokens = aValidUserWithTokens();
        when(tokenService.generateAuthorizationTokens(any(User.class), any(HttpServletRequest.class), eq(false), eq(deviceId))).thenReturn(
            userWithTokens
        );

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Tokens should be generated for the user with rememberMe=false and the device familyId
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(tokenService, times(1)).generateAuthorizationTokens(
            userCaptor.capture(),
            any(HttpServletRequest.class),
            eq(false),
            eq(deviceId)
        );
        final User userForTokens = userCaptor.getValue();
        assertThat(userForTokens.getEmail(), is(equalTo(VALID_EMAIL)));
    }

    @Test
    @DisplayName("Should set cookies with correct token values")
    public void shouldSetCookiesWithCorrectTokenValues() throws IOException {
        // Given: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: A valid user in the system
        final User user = aValidUser();
        when(userService.loadUserByUsername(VALID_EMAIL)).thenReturn(user);

        // And: Tokens are generated for the user (OAuth2 always uses rememberMe=false, passes familyId)
        final User userWithTokens = aValidUserWithTokens();
        when(tokenService.generateAuthorizationTokens(any(User.class), any(HttpServletRequest.class), eq(false), eq(deviceId))).thenReturn(
            userWithTokens
        );

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Cookies should be set with the correct tokens
        verify(cookieService, times(1)).setAuthCookies(
            eq(response),
            eq(userWithTokens.getAccessToken()),
            eq(userWithTokens.getRefreshToken())
        );
    }

    @Test
    @DisplayName("Should redirect to correct URL after authorization")
    public void shouldRedirectToCorrectUrlAfterAuthorization() throws IOException {
        // Given: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: A valid user in the system
        final User user = aValidUser();
        when(userService.loadUserByUsername(VALID_EMAIL)).thenReturn(user);

        // And: Tokens are generated for the user (OAuth2 always uses rememberMe=false, passes familyId)
        final User userWithTokens = aValidUserWithTokens();
        when(tokenService.generateAuthorizationTokens(any(User.class), any(HttpServletRequest.class), eq(false), eq(deviceId))).thenReturn(
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
    @DisplayName("Should capture device info from request on OAuth2 login")
    public void shouldCapturesDeviceInfoOnOAuth2Login() throws IOException {
        // Given: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: A valid user in the system
        final User user = aValidUser();
        when(userService.loadUserByUsername(VALID_EMAIL)).thenReturn(user);

        // And: Tokens are generated for the user (OAuth2 always uses rememberMe=false, passes familyId)
        final User userWithTokens = aValidUserWithTokens();
        when(
            tokenService.generateAuthorizationTokens(
                any(User.class),
                any(jakarta.servlet.http.HttpServletRequest.class),
                eq(false),
                eq(deviceId)
            )
        )
            .thenReturn(userWithTokens);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: generateAuthorizationTokens should be called with the HttpServletRequest, rememberMe=false, and the device familyId
        verify(tokenService, times(1)).generateAuthorizationTokens(
            any(User.class),
            eq(request),
            eq(false),
            eq(deviceId)
        );
    }
}
