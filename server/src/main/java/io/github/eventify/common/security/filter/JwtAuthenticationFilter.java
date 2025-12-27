package io.github.eventify.common.security.filter;

import io.github.eventify.api.authentication.service.CookieService;
import io.github.eventify.api.token.service.JwtService;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.eventify.common.exception.AuthorizationException;
import io.github.eventify.common.security.principal.JwtUserPrincipalAuthenticationToken;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.jframe.exception.ApiException;
import io.github.jframe.exception.resource.ErrorResponseResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import static io.github.eventify.api.Paths.LOGOUT_PATH;
import static io.github.eventify.common.config.RequestMatcherConfig.getPublicMatchers;
import static io.github.eventify.common.constant.Constants.Security.*;
import static io.github.eventify.common.exception.ApiErrorCode.INVALID_TOKEN_ERROR;
import static io.github.eventify.common.exception.ApiErrorCode.USER_LOCKED_ERROR;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StringUtils.hasText;

/**
 * The filter to extract the JWT token from the request and set the security context, when the token is validated against the user and the
 * expiration date. The filter is only applied once per request. When there is already an authentication object in the security context, the
 * filter is skipped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings(
    {
        "ReturnCount",
        "ClassFanOutComplexity",
        "PMD.ExcessiveImports"
    }
)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenService tokenService;
    private final UserService userService;
    private final CookieService cookieService;

    @Override
    protected boolean shouldNotFilter(@NonNull final HttpServletRequest request) {
        final boolean isPublic = getPublicMatchers().stream().anyMatch(matcher -> matcher.matches(request));
        final boolean isLogout = request.getRequestURI().startsWith(LOGOUT_PATH);
        return (isPublic) && !isLogout;
    }

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain) throws ServletException, IOException {

        try {
            final User authenticatedUser = authenticateRequest(request, response);
            if (nonNull(authenticatedUser)) {
                if (!isUserRestricted(authenticatedUser, response)) {
                    filterChain.doFilter(request, response);
                }
                return;
            }

            log.debug("No valid authentication found. Clearing security context.");
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        } catch (final ApiException apiException) {
            SecurityContextHolder.clearContext();
            cookieService.clearAuthCookies(response);
            respondWithError(response, INVALID_TOKEN_ERROR, HttpStatus.UNAUTHORIZED, apiException.getMessage());
        }
    }

    private User authenticateRequest(final HttpServletRequest request, final HttpServletResponse response) {
        // Try both auth methods
        final String accessToken = extractJwtFromHeader(request);
        final String accessTokenFromCookie = extractJwtFromCookies(request, ACCESS_TOKEN_COOKIE);

        // Prefer header token over cookie if both exist
        final String tokenToUse = hasText(accessToken) ? accessToken : accessTokenFromCookie;
        if (hasText(tokenToUse)) {
            final User authenticatedUser = tryAuthenticateWithAccessToken(tokenToUse);
            if (nonNull(authenticatedUser) && !isUserRestricted(authenticatedUser, response)) {
                setSecurityContext(authenticatedUser, tokenToUse, request);
                return authenticatedUser;
            }
        }

        // Try refresh token if access token failed
        final String refreshToken = extractJwtFromCookies(request, REFRESH_TOKEN_COOKIE);
        if (hasText(refreshToken)) {
            return tryRefreshTokens(refreshToken, response, request);
        }

        return null;
    }

    private User tryAuthenticateWithAccessToken(final String accessToken) {
        try {
            final String email = jwtService.extractSubject(accessToken);
            final User user = userService.loadUserByUsername(email);
            if (tokenService.isValidAccessToken(accessToken, user)) {
                return user;
            }
        } catch (final ApiException ex) {
            log.debug("Access token authentication failed: {}", ex.getMessage());
        }
        return null;
    }

    private User tryRefreshTokens(final String refreshToken, final HttpServletResponse response, final HttpServletRequest request) {
        try {
            if (!jwtService.isTokenExpired(refreshToken)) {
                final User refreshedUser = tokenService.refresh(refreshToken);
                if (nonNull(refreshedUser)) {
                    cookieService.setAuthCookies(
                        response,
                        refreshedUser.getAccessToken(),
                        refreshedUser.getRefreshToken()
                    );
                    setSecurityContext(refreshedUser, refreshedUser.getAccessToken().getValue(), request);
                    return refreshedUser;
                }
            }
        } catch (final ApiException apiException) {
            log.debug("Token refresh failed: {}", apiException.getMessage());
        }
        return null;
    }

    private String extractJwtFromHeader(final HttpServletRequest request) {
        final String authHeader = request.getHeader(AUTHORIZATION);
        if (hasText(authHeader) && authHeader.startsWith(BEARER)) {
            return authHeader.substring(BEARER.length()).trim();
        }
        return null;
    }

    private String extractJwtFromCookies(final HttpServletRequest request, final String cookieName) {
        if (isNull(request.getCookies())) {
            return null;
        }

        for (final Cookie cookie : request.getCookies()) {
            if (Objects.equals(cookie.getName(), cookieName)) {
                log.debug("JWT token found in cookie: {}", cookie.getName());
                return cookie.getValue();
            }
        }
        return null;
    }

    private void setSecurityContext(final User user, final String tokenValue, final HttpServletRequest request) {
        final UserTokenPrincipal principal = new UserTokenPrincipal(user, tokenValue);

        final JwtUserPrincipalAuthenticationToken authentication = new JwtUserPrincipalAuthenticationToken(
            principal,
            user.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authentication successful for user '{}'. Setting security context.", user.getUsername());
    }

    private boolean isUserRestricted(final User user, final HttpServletResponse response) {
        if (!user.isEnabled()) {
            log.debug("User '{}' is disabled.", user.getUsername());
            respondWithError(response, USER_LOCKED_ERROR, HttpStatus.FORBIDDEN, "User account is disabled.");
            return true;
        }
        return false;
    }

    private void respondWithError(
        final HttpServletResponse response,
        final ApiErrorCode errorCode,
        final HttpStatus status,
        final String message) {

        try {
            final ErrorResponseResource errorResponse = new ErrorResponseResource(new AuthorizationException(errorCode));
            errorResponse.setErrorMessage(message);
            errorResponse.setStatusCode(status.value());
            errorResponse.setStatusMessage(status.getReasonPhrase());
            errorResponse.setContentType(APPLICATION_JSON_VALUE);

            response.setStatus(status.value());
            response.setContentType(APPLICATION_JSON_VALUE);

            final ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            response.getWriter().write(writer.writeValueAsString(errorResponse));
        } catch (final IOException ex) {
            log.error("Error while writing the error response: {}", ex.getMessage(), ex);
        }
    }
}
