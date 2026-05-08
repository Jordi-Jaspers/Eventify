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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OAuth2AuthenticationSuccessHandler} - processAuthentication method.
 */
@DisplayName("Unit Test - OAuth2AuthenticationSuccessHandler - Process Authentication")
public class OAuth2AuthenticationSuccessHandlerProcessAuthenticationTest extends UnitTest {

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
        OAuth2AttributesHolder.clear();

        handler = new OAuth2AuthenticationSuccessHandler(
            tokenService,
            userService,
            cookieService,
            redirectHelper
        );
        handler.setRedirectStrategy(redirectStrategy);

        // Default: device cookie is present
        deviceId = UUID.randomUUID();
        when(cookieService.readDeviceId(any(HttpServletRequest.class))).thenReturn(Optional.of(deviceId));
    }

    @Test
    @DisplayName("Should process authentication successfully when email is valid")
    public void shouldProcessAuthenticationSuccessfullyWhenEmailIsValid() throws IOException {
        // Given: Redirect helper returns success URL
        when(redirectHelper.buildRedirectUrl()).thenReturn(APPLICATION_URL + "/oauth2/redirect");

        // And: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: A valid user in the system
        final User user = aValidUser();
        when(userService.loadUserByUsername(VALID_EMAIL)).thenReturn(user);
        when(tokenService.generateAuthorizationTokens(any(User.class), any(HttpServletRequest.class), anyBoolean(), eq(deviceId)))
            .thenReturn(user);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: The user should be loaded by username
        verify(userService, times(1)).loadUserByUsername(VALID_EMAIL);

        // And: The user should be updated
        verify(userService, times(1)).updateUserDetails(any(User.class));

        // And: Tokens should be generated with rememberMe=false and the device familyId (OAuth2 never extends lifetime)
        verify(tokenService, times(1)).generateAuthorizationTokens(any(User.class), any(HttpServletRequest.class), eq(false), eq(deviceId));

        // And: Cookies should be set
        verify(cookieService, times(1)).setAuthCookies(any(HttpServletResponse.class), any(), any());

        // And: No error redirect should occur
        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy, times(1)).sendRedirect(any(), any(), urlCaptor.capture());
        final String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl, containsString("/oauth2/redirect"));
    }

    @Test
    @DisplayName("Should handle failure when email is null")
    public void shouldHandleFailureWhenEmailIsNull() throws IOException {
        // Given: An OAuth2 user without email
        final OAuth2User oAuth2User = createMockOAuth2User(null);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: No user should be loaded
        verify(userService, never()).loadUserByUsername(any());

        // And: No tokens should be generated
        verify(tokenService, never()).generateAuthorizationTokens(
            any(User.class),
            any(HttpServletRequest.class),
            anyBoolean(),
            any(UUID.class)
        );

        // And: Redirect helper should be called with error
        verify(redirectHelper, times(1)).buildRedirectUrl(
            eq("Email not found from OAuth2 provider")
        );
    }

    @Test
    @DisplayName("Should handle failure when exception occurs during processing")
    public void shouldHandleFailureWhenExceptionOccursDuringProcessing() throws IOException {
        // Given: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: User service throws an exception
        when(userService.loadUserByUsername(VALID_EMAIL)).thenThrow(new RuntimeException("Database error"));

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Redirect helper should be called with error
        verify(redirectHelper, times(1)).buildRedirectUrl(
            eq("Authentication processing failed")
        );

        // And: No tokens should be generated
        verify(tokenService, never()).generateAuthorizationTokens(
            any(User.class),
            any(HttpServletRequest.class),
            anyBoolean(),
            any(UUID.class)
        );
    }

    @Test
    @DisplayName("Should handle failure when principal is not OAuth2User")
    public void shouldHandleFailureWhenPrincipalIsNotOAuth2User() throws IOException {
        // Given: An authentication with invalid principal
        when(authentication.getPrincipal()).thenReturn("invalid-principal");
        when(response.isCommitted()).thenReturn(false);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Redirect helper should be called with error
        verify(redirectHelper, times(1)).buildRedirectUrl(
            eq("Authentication processing failed")
        );

        // And: No user should be loaded
        verify(userService, never()).loadUserByUsername(any());
    }
}
