package org.jordijaspers.eventify.common.security.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hawaiiframework.web.resource.ErrorResponseResource;
import org.jordijaspers.eventify.api.source.model.ApiKey;
import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.source.repository.SourceRepository;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.common.exception.AuthorizationException;
import org.jordijaspers.eventify.common.security.principal.SourceTokenPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import static java.util.Objects.isNull;
import static org.jordijaspers.eventify.common.config.RequestMatcherConfig.getExternalApiMatcher;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Filter to authenticate the request using the API key.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("ReturnCount")
public final class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final SourceRepository sourceRepository;

    /**
     * Filter out the requests that are not external API requests.
     *
     * @param request current HTTP request
     * @return true if the request should not be filtered, false otherwise
     */
    @Override
    public boolean shouldNotFilter(@NonNull final HttpServletRequest request) {
        return getExternalApiMatcher().stream().noneMatch(matcher -> matcher.matches(request));
    }

    /**
     * {@inheritDoc}
     *
     * @param request  The request to filter.
     * @param response The response to filter.
     * @param chain    The filter chain to continue the request with.
     * @throws ServletException If the request could not be processed.
     * @throws IOException      If the request could not be processed.
     */
    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain chain) throws ServletException, IOException {
        final String headerValue = request.getHeader(AUTHORIZATION);
        if (isNull(headerValue) || !headerValue.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        final String token = headerValue.substring(BEARER_PREFIX.length());
        final Optional<Source> source = sourceRepository.findByToken(token);
        if (source.isEmpty()) {
            SecurityContextHolder.clearContext();
            respondWithError(response, ApiErrorCode.INVALID_API_KEY_ERROR, HttpStatus.UNAUTHORIZED, "No valid authentication found.");
            return;
        }

        final Source sourceValue = source.get();
        if (!isSourceRestricted(sourceValue, response)) {
            return;
        }

        authenticateRequest(sourceValue);
        chain.doFilter(request, response);
    }

    private void authenticateRequest(final Source source) {
        try {
            final SourceTokenPrincipal authentication = new SourceTokenPrincipal(source);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (final AuthenticationException ex) {
            log.error("Authentication request failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isSourceRestricted(final Source source, final HttpServletResponse response) {
        final ApiKey apiKey = source.getApiKey();
        if (!apiKey.isEnabled()) {
            log.debug("Source '{}' is disabled.", source.getName());
            respondWithError(response, ApiErrorCode.API_KEY_DISABLED_ERROR, HttpStatus.FORBIDDEN, "API key is disabled.");
            return true;
        }

        if (apiKey.isExpired()) {
            log.debug("Source '{}' is expired.", source.getName());
            respondWithError(response, ApiErrorCode.API_KEY_EXPIRED_ERROR, HttpStatus.FORBIDDEN, "API key is expired.");
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
