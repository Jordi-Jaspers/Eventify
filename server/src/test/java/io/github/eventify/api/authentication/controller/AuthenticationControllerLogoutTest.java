package io.github.eventify.api.authentication.controller;

import io.github.eventify.api.authentication.model.validator.AuthenticationValidator;
import io.github.eventify.api.authentication.service.AuthenticationService;
import io.github.eventify.api.authentication.service.CookieService;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.mapper.UserDetailsMapper;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.eventify.support.UnitTest;

import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - AuthenticationController Logout")
public class AuthenticationControllerLogoutTest extends UnitTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationValidator validator;

    @Mock
    private CookieService cookieService;

    @Mock
    private UserDetailsMapper userDetailsMapper;

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    private AuthenticationController controller;

    @BeforeEach
    public void setUp() {
        controller = new AuthenticationController(authenticationService, validator, cookieService, userDetailsMapper);
    }

    @Test
    @DisplayName("Should fall back to cookie when principal refreshTokenId is null")
    public void logoutFallsBackToCookieWhenPrincipalRefreshTokenIdIsNull() {
        // Given: A principal with no refreshTokenId (e.g. authenticated via access token only)
        final User user = aValidUser();
        final UserTokenPrincipal principal = new UserTokenPrincipal(user, ACCESS_TOKEN_VALUE, null);

        // And: A refresh token value is present in the cookie
        final String refreshTokenValue = REFRESH_TOKEN_VALUE;
        when(cookieService.readRefreshTokenValue(httpServletRequest)).thenReturn(Optional.of(refreshTokenValue));

        // When: Logging out
        controller.logout(principal, httpServletRequest, httpServletResponse);

        // Then: The cookie fallback should be used to resolve the refresh token
        verify(cookieService).readRefreshTokenValue(httpServletRequest);

        // And: Auth cookies should be cleared
        verify(cookieService).clearAuthCookies(httpServletResponse);

        // And: The authentication service logout should be called
        verify(authenticationService).logout(principal, httpServletRequest);
    }

    @Test
    @DisplayName("Should clear auth cookies on logout even when principal is null")
    public void logoutClearsCookiesWhenPrincipalIsNull() {
        // Given: No authenticated principal (anonymous request)
        // When: Logging out without a principal
        controller.logout(null, httpServletRequest, httpServletResponse);

        // Then: Auth cookies should still be cleared
        verify(cookieService).clearAuthCookies(httpServletResponse);
    }
}
