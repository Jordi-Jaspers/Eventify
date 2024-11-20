package org.jordijaspers.eventify.common.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.RequiredArgsConstructor;
import org.hawaiiframework.exception.ApiException;
import org.hawaiiframework.web.resource.ErrorResponseResource;
import org.jordijaspers.eventify.api.token.service.JwtService;
import org.jordijaspers.eventify.api.token.service.TokenService;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.service.UserService;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.common.exception.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.jordijaspers.eventify.common.config.RequestMatcherConfig.getPublicMatchers;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.startsWithIgnoreCase;

/**
 * The filter to extract the JWT token from the request and set the security context, when the token is validated against the user and the
 * expiration date. The filter is only applied once per request. When there is already an authentication object in the security context, the
 * filter is skipped.
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("ReturnCount")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String BEARER = "bearer ";

    private final JwtService jwtService;

    private final TokenService tokenService;

    private final UserService userService;

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
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain) throws ServletException, IOException, ApiException {
        LOGGER.debug("Processing authentication for '{}'", request.getRequestURL());
        final String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (!startsWithIgnoreCase(authorizationHeader, BEARER)) {
            LOGGER.debug("No JWT token found in request headers.");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authorizationHeader.substring(BEARER.length());
        if (hasText(jwt)) {
            try {
                LOGGER.debug("Found JWT token in request headers: '{}'", jwt);
                final String email = jwtService.extractSubject(jwt);
                final User user = userService.loadUserByUsername(email);
                if (tokenService.isValidAccessToken(jwt, user)) {
                    if (isUserAllowed(request, response, filterChain, user)) {
                        return;
                    }

                    LOGGER.debug("Authentication successful for '{}', setting security context.", user.getUsername());
                    SecurityContextHolder.getContext().setAuthentication(getAuthentication(user, request));
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (final ApiException exception) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        LOGGER.debug("Authentication failed because of an invalid token.");
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(final User user, final HttpServletRequest request) {
        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            user,
            user.getRoles(),
            user.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }

    private boolean isUserAllowed(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain,
        final User user) throws IOException, ServletException {
        if (!user.isEnabled()) {
            LOGGER.debug("User '{}' is currently disabled.", user.getUsername());
            handleAccountRestriction(request, response, ApiErrorCode.USER_DISABLED_ERROR);
            filterChain.doFilter(request, response);
            return true;
        }

        if (!user.isValidated()) {
            LOGGER.debug("User '{}' is not validated.", user.getUsername());
            handleAccountRestriction(request, response, ApiErrorCode.USER_UNVALIDATED_ERROR);
            filterChain.doFilter(request, response);
            return true;
        }
        return false;
    }

    private void handleAccountRestriction(final HttpServletRequest request, final HttpServletResponse response, final ApiErrorCode code) {
        try {
            final HttpStatus status = HttpStatus.FORBIDDEN;
            final AuthorizationException exception = new AuthorizationException(code);

            final ErrorResponseResource errorResponse = new ErrorResponseResource(exception);
            errorResponse.setErrorMessage(exception.getMessage());
            errorResponse.setStatusCode(status.value());
            errorResponse.setStatusMessage(status.getReasonPhrase());
            errorResponse.setMethod(request.getMethod());
            errorResponse.setUri(request.getRequestURI());
            errorResponse.setContentType(APPLICATION_JSON_VALUE);
            errorResponse.setQuery(request.getQueryString());

            final ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            response.getWriter().write(writer.writeValueAsString(errorResponse));
            response.setStatus(errorResponse.getStatusCode());
            response.setContentType(errorResponse.getContentType());
        } catch (final IOException exception) {
            LOGGER.error("Error while writing the error response.", exception);
        }
    }
}
