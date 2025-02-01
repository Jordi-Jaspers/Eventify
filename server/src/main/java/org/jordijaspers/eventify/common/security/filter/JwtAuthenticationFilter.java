package org.jordijaspers.eventify.common.security.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hawaiiframework.exception.ApiException;
import org.hawaiiframework.web.resource.ErrorResponseResource;
import org.jordijaspers.eventify.api.authentication.service.CookieService;
import org.jordijaspers.eventify.api.token.service.JwtService;
import org.jordijaspers.eventify.api.token.service.TokenService;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.service.UserService;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.common.exception.AuthorizationException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.jordijaspers.eventify.common.config.RequestMatcherConfig.getPublicMatchers;
import static org.jordijaspers.eventify.common.constants.Constants.Security.ACCESS_TOKEN_COOKIE;
import static org.jordijaspers.eventify.common.constants.Constants.Security.BEARER;
import static org.jordijaspers.eventify.common.constants.Constants.Security.REFRESH_TOKEN_COOKIE;
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
@SuppressWarnings("ReturnCount")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final TokenService tokenService;

    private final UserService userService;

    private final CookieService cookieService;

    /**
     * The matchers to exclude from the filter.
     *
     * @param request current HTTP request
     * @return true if the filter should not be applied, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(@NonNull final HttpServletRequest request) {
        return getPublicMatchers().stream()
            .anyMatch(matcher -> matcher.matches(request));
    }

    /**
     * The filter implementation. It extracts the JWT token from the request and sets the security context when the token.
     *
     * @param request     The inbound HTTP request.
     * @param response    The Outbound HTTP response.
     * @param filterChain The spring filter chain.
     * @throws ServletException When something goes wrong in the servlet.
     * @throws IOException      When something goes wrong in the IO.
     * @throws ApiException     When something goes wrong in the API.
     */
    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain) throws ServletException, IOException {
        String accessToken = extractJwtFromHeader(request);
        if (!hasText(accessToken)) {
            accessToken = extractJwtFromCookies(request, ACCESS_TOKEN_COOKIE);
        }

        try {
            if (hasText(accessToken)) {
                final String email = jwtService.extractSubject(accessToken);
                final User user = userService.loadUserByUsername(email);
                if (tokenService.isValidAccessToken(accessToken, user)) {
                    if (!isUserRestricted(user, response)) {
                        setSecurityContext(user, request);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    return;
                } else {
                    final String refreshToken = extractJwtFromCookies(request, REFRESH_TOKEN_COOKIE);
                    if (hasText(refreshToken) && !jwtService.isTokenExpired(refreshToken)) {
                        final User refreshedUser = tokenService.refresh(refreshToken);
                        if (nonNull(refreshedUser)) {
                            cookieService.setAuthCookies(response, refreshedUser.getAccessToken(), refreshedUser.getRefreshToken());
                            setSecurityContext(refreshedUser, request);
                            filterChain.doFilter(request, response);
                            return;
                        }
                    }
                }
            }

            log.debug("No valid authentication tokens found");
            SecurityContextHolder.clearContext();
        } catch (final ApiException ex) {
            log.warn("Authentication failed due to exception: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            respondWithError(response, ApiErrorCode.INVALID_TOKEN_ERROR, HttpStatus.UNAUTHORIZED, "Invalid token.");
        }
        filterChain.doFilter(request, response);
    }

    private String extractJwtFromHeader(final HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER)) {
            return authorizationHeader.substring(BEARER.length()).trim();
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

    private void setSecurityContext(final User user, final HttpServletRequest request) {
        log.debug("Authentication successful for user '{}'. Setting security context.", user.getUsername());
        SecurityContextHolder.getContext().setAuthentication(getAuthentication(user, request));
    }

    private UsernamePasswordAuthenticationToken getAuthentication(final User user, final HttpServletRequest request) {
        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            user,
            user.getRole(),
            user.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }

    private boolean isUserRestricted(final User user, final HttpServletResponse response) {
        if (!user.isEnabled()) {
            log.debug("User '{}' is disabled.", user.getUsername());
            respondWithError(response, ApiErrorCode.USER_LOCKED_ERROR, HttpStatus.FORBIDDEN, "User account is disabled.");
            return true;
        }

        if (!user.isValidated()) {
            log.debug("User '{}' is not validated.", user.getUsername());
            respondWithError(response, ApiErrorCode.USER_UNVALIDATED_ERROR, HttpStatus.FORBIDDEN, "User account is not validated.");
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
