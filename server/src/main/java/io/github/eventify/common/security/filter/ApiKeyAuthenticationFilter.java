package io.github.eventify.common.security.filter;

import io.github.eventify.api.apikey.service.ApiKeyAuthenticationService;
import io.github.eventify.common.security.principal.ApiKeyAuthenticationToken;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import static io.github.eventify.api.Paths.CHANNELS_PATH;
import static io.github.eventify.api.Paths.EVENTS_PATH;
import static org.springframework.util.StringUtils.hasText;

/**
 * Filter to authenticate requests using API keys. Only processes requests to /v1/events or /v1/channels with X-Api-Key header.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-Api-Key";

    private final ApiKeyAuthenticationService apiKeyAuthenticationService;

    @Override
    protected boolean shouldNotFilter(@NonNull final HttpServletRequest request) {
        final String apiKey = request.getHeader(API_KEY_HEADER);
        final String requestUri = request.getRequestURI();
        return !hasText(apiKey) || (!requestUri.startsWith(EVENTS_PATH) && !requestUri.startsWith(CHANNELS_PATH));
    }

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain) {
        final String apiKey = request.getHeader(API_KEY_HEADER);
        try {
            final ApiKeyPrincipal principal = apiKeyAuthenticationService.authenticate(apiKey);
            setSecurityContext(principal, request);

            log.debug("API key authentication successful for user: {}", principal.getUser().getUsername());
            filterChain.doFilter(request, response);
        } catch (final Exception ex) {
            log.debug("API key authentication failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            UnauthorizedHandler.handleAuthenticationFailure(request, response, ex.getMessage());
        }
    }

    private void setSecurityContext(final ApiKeyPrincipal principal, final HttpServletRequest request) {
        final ApiKeyAuthenticationToken authentication = new ApiKeyAuthenticationToken(
            principal,
            principal.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
