package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.authentication.service.CookieService;
import io.github.eventify.api.token.service.TokenService;
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
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OAuth2AuthenticationSuccessHandler} - handleFailure method.
 */
@DisplayName("Unit Test - OAuth2AuthenticationSuccessHandler - Handle Failure")
public class OAuth2AuthenticationSuccessHandlerHandleFailureTest extends UnitTest {

    private static final String ERROR_MESSAGE_EMAIL_NOT_FOUND = "Email not found from OAuth2 provider";
    private static final String ERROR_MESSAGE_AUTH_FAILED = "Authentication processing failed";

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
        OAuth2AttributesHolder.clear();

        handler = new OAuth2AuthenticationSuccessHandler(
            tokenService,
            userService,
            cookieService,
            redirectHelper
        );
        handler.setRedirectStrategy(redirectStrategy);

        // Prevent NPE in processAuthentication when wrapping null deviceId in Optional
        when(cookieService.readDeviceId(any(HttpServletRequest.class))).thenReturn(Optional.of(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Should call redirect helper with error message when email is null")
    public void shouldCallRedirectHelperWithErrorMessageWhenEmailIsNull() throws IOException {
        // Given: An OAuth2 user without email
        final OAuth2User oAuth2User = createMockOAuth2User(null);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Redirect helper should be called with error message
        verify(redirectHelper, times(1)).buildRedirectUrl(
            eq(ERROR_MESSAGE_EMAIL_NOT_FOUND)
        );
    }

    @Test
    @DisplayName("Should call redirect helper when exception occurs during processing")
    public void shouldCallRedirectHelperWhenExceptionOccursDuringProcessing() throws IOException {
        // Given: A valid OAuth2 user with email
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // And: User service throws an exception
        when(userService.loadUserByUsername(VALID_EMAIL)).thenThrow(new RuntimeException("Database error"));

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Redirect helper should be called with error message
        verify(redirectHelper, times(1)).buildRedirectUrl(
            eq(ERROR_MESSAGE_AUTH_FAILED)
        );
    }

    @Test
    @DisplayName("Should call redirect helper with appropriate error message")
    public void shouldCallRedirectHelperWithAppropriateErrorMessage() throws IOException {
        // Given: An OAuth2 user without email
        final OAuth2User oAuth2User = createMockOAuth2User(null);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Redirect helper should be called with email not found message
        verify(redirectHelper, times(1)).buildRedirectUrl(any());
    }

    @Test
    @DisplayName("Should not call token service when handling failure")
    public void shouldNotCallTokenServiceWhenHandlingFailure() throws IOException {
        // Given: An OAuth2 user without email
        final OAuth2User oAuth2User = createMockOAuth2User(null);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Token service should not be called
        verify(tokenService, never()).generateAuthorizationTokens(any(), any());

        // And: Cookie service should not be called
        verify(cookieService, never()).setAuthCookies(any(), any(), any());
    }

    @Test
    @DisplayName("Should not call user service update when handling failure")
    public void shouldNotCallUserServiceUpdateWhenHandlingFailure() throws IOException {
        // Given: An OAuth2 user without email
        final OAuth2User oAuth2User = createMockOAuth2User(null);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: User service update should not be called
        verify(userService, never()).updateUserDetails(any());

        // And: User service load should not be called
        verify(userService, never()).loadUserByUsername(any());
    }

    @Test
    @DisplayName("Should use redirect helper for error redirect")
    public void shouldUseRedirectHelperForErrorRedirect() throws IOException {
        // Given: An OAuth2 user without email
        final OAuth2User oAuth2User = createMockOAuth2User(null);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(response.isCommitted()).thenReturn(false);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: Redirect helper should be called
        verify(redirectHelper, times(1)).buildRedirectUrl(any());
    }
}
