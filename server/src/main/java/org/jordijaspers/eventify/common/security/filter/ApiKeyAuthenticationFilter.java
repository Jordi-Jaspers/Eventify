package org.jordijaspers.eventify.common.security.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import static java.util.Objects.isNull;
import static org.jordijaspers.eventify.common.config.RequestMatcherConfig.getExternalApiMatcher;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Filter to authenticate the request using the API key.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

    private final SourceRepository sourceRepository;

    @Override
    protected boolean shouldNotFilter(@NonNull final HttpServletRequest request) {
        return getExternalApiMatcher().stream().noneMatch(matcher -> matcher.matches(request));
    }

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain chain) throws ServletException, IOException {

        try {
            final String token = request.getHeader(API_KEY_HEADER);
            if (isNull(token)) {
                chain.doFilter(request, response);
                return;
            }

            authenticateSourceWithToken(token);
            chain.doFilter(request, response);
        } catch (final AuthorizationException ex) {
            SecurityContextHolder.clearContext();
            handleAuthorizationError(response, ex);
        }
    }

    private void authenticateSourceWithToken(final String token) throws AuthorizationException {
        try {
            final Source source = sourceRepository.findByToken(token)
                .orElseThrow(() -> new AuthorizationException(ApiErrorCode.INVALID_API_KEY_ERROR));

            validateSource(source);
            final SourceTokenPrincipal authentication = new SourceTokenPrincipal(source);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Successfully authenticated source: {}", source.getName());
        } catch (final Exception ex) {
            throw new AuthorizationException(ApiErrorCode.INVALID_API_KEY_ERROR);
        }
    }

    private void validateSource(final Source source) {
        final ApiKey apiKey = source.getApiKey();
        if (!apiKey.isEnabled()) {
            throw new AuthorizationException(ApiErrorCode.API_KEY_DISABLED_ERROR);
        }

        if (apiKey.isExpired()) {
            throw new AuthorizationException(ApiErrorCode.API_KEY_EXPIRED_ERROR);
        }
    }

    private void handleAuthorizationError(final HttpServletResponse response, final AuthorizationException authorizationException) {
        try {
            final ErrorResponseResource errorResponse = new ErrorResponseResource(authorizationException);
            errorResponse.setErrorMessage(authorizationException.getMessage());
            errorResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
            errorResponse.setStatusMessage(HttpStatus.UNAUTHORIZED.getReasonPhrase());
            errorResponse.setContentType(APPLICATION_JSON_VALUE);

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(APPLICATION_JSON_VALUE);

            final ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            response.getWriter().write(writer.writeValueAsString(errorResponse));
        } catch (final IOException exception) {
            log.error("Error while writing the error response: {}", exception.getMessage(), exception);
        }
    }
}
