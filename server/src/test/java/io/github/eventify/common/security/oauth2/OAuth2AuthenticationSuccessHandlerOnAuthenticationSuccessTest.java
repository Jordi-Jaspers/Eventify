package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.authentication.service.CookieService;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.support.UnitTest;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;

import static org.mockito.Mockito.*;

/**
 * Unit tests for
 * {@link OAuth2AuthenticationSuccessHandler#onAuthenticationSuccess(HttpServletRequest, HttpServletResponse, Authentication)}.
 */
@DisplayName("Unit Test - OAuth2AuthenticationSuccessHandler - On Authentication Success")
public class OAuth2AuthenticationSuccessHandlerOnAuthenticationSuccessTest extends UnitTest {

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

    @Test
    @DisplayName("Should process authentication when response is not committed")
    public void shouldProcessAuthenticationWhenResponseIsNotCommitted() throws IOException {
        // Given: A response that is not committed
        when(response.isCommitted()).thenReturn(false);

        // And: A valid OAuth2 authentication
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: The authentication principal should be accessed
        verify(authentication, atLeastOnce()).getPrincipal();

        // And: The response should be checked for committed status
        verify(response, times(1)).isCommitted();
    }

    @Test
    @DisplayName("Should skip processing when response is already committed")
    public void shouldSkipProcessingWhenResponseIsAlreadyCommitted() throws IOException {
        // Given: A response that is already committed
        when(response.isCommitted()).thenReturn(true);

        // When: Handling authentication success
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then: The response should be checked for committed status
        verify(response, times(1)).isCommitted();

        // And: The authentication principal should not be accessed
        verify(authentication, never()).getPrincipal();

        // And: No redirect should be performed
        verify(redirectStrategy, never()).sendRedirect(any(), any(), any());
    }
}
