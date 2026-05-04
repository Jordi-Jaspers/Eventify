package io.github.eventify.common.security.filter;

import io.github.eventify.api.authentication.service.CookieService;
import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.service.JwtService;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.security.principal.JwtUserPrincipalAuthenticationToken;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import static io.github.eventify.common.constant.Constants.Security.ACCESS_TOKEN_COOKIE;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.eventify.common.constant.Constants.Security.REFRESH_TOKEN_COOKIE;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@DisplayName("Unit Test - JWT Authentication Filter")
@MockitoSettings(strictness = Strictness.LENIENT)
public class JwtAuthenticationFilterTest extends UnitTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    @Mock
    private CookieService cookieService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(jwtService, tokenService, userService, cookieService);
    }

    @Test
    @DisplayName("Should populate refreshTokenId on principal when refresh cookie is present")
    public void populatesRefreshTokenIdOnPrincipalWhenRefreshCookiePresent() throws Exception {
        // Given: A user with a valid refresh token in the DB
        final User user = aValidUser();
        final Long expectedRefreshTokenId = 42L;
        final String refreshTokenValue = "valid-refresh-token";

        final Token refreshToken = Token.builder()
            .id(expectedRefreshTokenId)
            .value(refreshTokenValue)
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();

        // And: No access token in header or cookie (only refresh cookie)
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/v1/user/details");
        request.setRequestURI("/v1/user/details");
        request.setCookies(new jakarta.servlet.http.Cookie(REFRESH_TOKEN_COOKIE, refreshTokenValue));

        // And: The refresh token is not expired and resolves to a user with the rotated refresh token attached
        when(jwtService.isTokenExpired(refreshTokenValue)).thenReturn(false);
        user.setRefreshToken(refreshToken);
        when(tokenService.refresh(refreshTokenValue, request)).thenReturn(user);

        // When: The filter processes the request
        filter.doFilterInternal(request, response, filterChain);

        // Then: The security context should have a principal with the refreshTokenId set
        final JwtUserPrincipalAuthenticationToken authentication =
            (JwtUserPrincipalAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication, is(notNullValue()));

        final UserTokenPrincipal principal = (UserTokenPrincipal) authentication.getPrincipal();
        assertThat(principal.getRefreshTokenId(), is(equalTo(expectedRefreshTokenId)));
    }

    @Test
    @DisplayName("Should leave refreshTokenId null when only access cookie is present")
    public void leavesRefreshTokenIdNullWhenOnlyAccessCookiePresent() throws Exception {
        // Given: A user with a valid access token in a cookie (no refresh cookie)
        final User user = aValidUser();
        final String accessTokenValue = "valid-access-token";

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/v1/user/details");
        request.setRequestURI("/v1/user/details");
        request.setCookies(new jakarta.servlet.http.Cookie(ACCESS_TOKEN_COOKIE, accessTokenValue));

        // And: The access token is valid
        when(jwtService.extractSubject(accessTokenValue)).thenReturn(user.getEmail());
        when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(tokenService.isValidAccessToken(accessTokenValue, user)).thenReturn(true);

        // When: The filter processes the request
        filter.doFilterInternal(request, response, filterChain);

        // Then: The security context should have a principal with refreshTokenId = null
        final JwtUserPrincipalAuthenticationToken authentication =
            (JwtUserPrincipalAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication, is(notNullValue()));

        final UserTokenPrincipal principal = (UserTokenPrincipal) authentication.getPrincipal();
        assertThat(principal.getRefreshTokenId(), is(nullValue()));
    }

    @Test
    @DisplayName("Should leave refreshTokenId null when only Authorization header is present")
    public void leavesRefreshTokenIdNullWhenOnlyAuthorizationHeaderPresent() throws Exception {
        // Given: A user with a valid access token in the Authorization header (no cookies)
        final User user = aValidUser();
        final String accessTokenValue = "valid-access-token";

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/v1/user/details");
        request.setRequestURI("/v1/user/details");
        request.addHeader(AUTHORIZATION, BEARER + accessTokenValue);

        // And: The access token is valid
        when(jwtService.extractSubject(accessTokenValue)).thenReturn(user.getEmail());
        when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(tokenService.isValidAccessToken(accessTokenValue, user)).thenReturn(true);

        // When: The filter processes the request
        filter.doFilterInternal(request, response, filterChain);

        // Then: The security context should have a principal with refreshTokenId = null
        final JwtUserPrincipalAuthenticationToken authentication =
            (JwtUserPrincipalAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication, is(notNullValue()));

        final UserTokenPrincipal principal = (UserTokenPrincipal) authentication.getPrincipal();
        assertThat(principal.getRefreshTokenId(), is(nullValue()));
    }
}
