package io.github.eventify.common.security.oauth2;

import io.github.eventify.support.UnitTest;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.RedirectStrategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OAuth2AuthenticationFailureHandler}.
 */
@DisplayName("Unit Test - OAuth2AuthenticationFailureHandler")
public class OAuth2AuthenticationFailureHandlerTest extends UnitTest {

    private static final String ERROR_MESSAGE = "OAuth2 authentication failed";

    @Mock
    private OAuth2RedirectHelper redirectHelper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authenticationException;

    @Mock
    private RedirectStrategy redirectStrategy;

    private OAuth2AuthenticationFailureHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new OAuth2AuthenticationFailureHandler(redirectHelper);
        handler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    @DisplayName("Should redirect to login page with error message on authentication failure")
    public void shouldRedirectToLoginPageWithErrorMessageOnAuthenticationFailure() throws IOException {
        // Given: An authentication exception with error message
        when(authenticationException.getLocalizedMessage()).thenReturn(ERROR_MESSAGE);
        when(redirectHelper.buildRedirectUrl(ERROR_MESSAGE)).thenReturn(APPLICATION_URL + "/oauth2/redirect?error=" + ERROR_MESSAGE);

        // When: Handling authentication failure
        handler.onAuthenticationFailure(request, response, authenticationException);

        // Then: Redirect helper should be called with error message
        verify(redirectHelper, times(1)).buildRedirectUrl(
            eq(ERROR_MESSAGE)
        );

        // And: Redirect strategy should be called
        verify(redirectStrategy, times(1)).sendRedirect(
            eq(request),
            eq(response),
            anyString()
        );
    }

    @Test
    @DisplayName("Should call redirect helper on authentication failure")
    public void shouldCallRedirectHelperOnAuthenticationFailure() throws IOException {
        // Given: An authentication exception
        when(authenticationException.getLocalizedMessage()).thenReturn(ERROR_MESSAGE);
        when(redirectHelper.buildRedirectUrl(anyString())).thenReturn(APPLICATION_URL + "/oauth2/redirect?error=" + ERROR_MESSAGE);

        // When: Handling authentication failure
        handler.onAuthenticationFailure(request, response, authenticationException);

        // Then: Redirect helper should be called
        verify(redirectHelper, times(1)).buildRedirectUrl(anyString());

        // And: Redirect strategy should be called
        verify(redirectStrategy, times(1)).sendRedirect(
            any(HttpServletRequest.class),
            any(HttpServletResponse.class),
            anyString()
        );
    }

    @Test
    @DisplayName("Should handle authentication failure with null exception message")
    public void shouldHandleAuthenticationFailureWithNullExceptionMessage() throws IOException {
        // Given: An authentication exception with null message
        when(authenticationException.getLocalizedMessage()).thenReturn(null);
        when(redirectHelper.buildRedirectUrl(eq(null))).thenReturn(APPLICATION_URL + "/oauth2/redirect");

        // When: Handling authentication failure
        handler.onAuthenticationFailure(request, response, authenticationException);

        // Then: Redirect helper should be called with null message
        verify(redirectHelper, times(1)).buildRedirectUrl(
            eq(null)
        );

        // And: Redirect strategy should be called
        verify(redirectStrategy, times(1)).sendRedirect(
            eq(request),
            eq(response),
            anyString()
        );
    }

    @Test
    @DisplayName("Should pass exception message to redirect helper")
    public void shouldPassExceptionMessageToRedirectHelper() throws IOException {
        // Given: An authentication exception with specific error message
        final String specificError = "GitHub authentication failed: invalid credentials";
        when(authenticationException.getLocalizedMessage()).thenReturn(specificError);
        when(redirectHelper.buildRedirectUrl(specificError)).thenReturn(APPLICATION_URL + "/oauth2/redirect?error=" + specificError);

        // When: Handling authentication failure
        handler.onAuthenticationFailure(request, response, authenticationException);

        // Then: The specific error message should be passed to redirect helper
        verify(redirectHelper, times(1)).buildRedirectUrl(
            eq(specificError)
        );

        // And: Redirect strategy should be called
        verify(redirectStrategy, times(1)).sendRedirect(
            eq(request),
            eq(response),
            anyString()
        );
    }

    @Test
    @DisplayName("Should not throw exception when handling failure")
    public void shouldNotThrowExceptionWhenHandlingFailure() throws IOException {
        // Given: An authentication exception
        when(authenticationException.getLocalizedMessage()).thenReturn(ERROR_MESSAGE);
        when(redirectHelper.buildRedirectUrl(any())).thenReturn(APPLICATION_URL + "/oauth2/redirect?error=" + ERROR_MESSAGE);

        // When: Handling authentication failure
        handler.onAuthenticationFailure(request, response, authenticationException);

        // Then: No exception should be thrown (test completes successfully)
        verify(redirectHelper, times(1)).buildRedirectUrl(any());

        // And: Redirect strategy should be called
        verify(redirectStrategy, times(1)).sendRedirect(
            any(HttpServletRequest.class),
            any(HttpServletResponse.class),
            anyString()
        );
    }
}
