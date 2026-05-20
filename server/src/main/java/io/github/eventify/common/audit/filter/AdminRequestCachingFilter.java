package io.github.eventify.common.audit.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import static io.github.eventify.api.Paths.ADMIN_PATH;

/**
 * Filter that wraps admin requests in ContentCachingRequestWrapper
 * so the interceptor can read the body after the controller has consumed it.
 * Registered in the Spring Security filter chain before JwtAuthenticationFilter.
 */
@Component
public class AdminRequestCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final @NonNull HttpServletResponse response,
        final @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().contains(ADMIN_PATH)) {
            final ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 4096);
            filterChain.doFilter(wrappedRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
