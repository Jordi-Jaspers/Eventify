package io.github.eventify.common.audit.interceptor;

import io.github.eventify.api.user.model.User;
import io.github.eventify.common.audit.event.AuditEvent;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.common.util.DeviceInfoExtractor;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Intercepts admin requests and publishes audit events for tracking.
 */
@RequiredArgsConstructor
public class AdminAuditInterceptor implements HandlerInterceptor {

    private static final int MAX_PATH_LENGTH = 512;

    private static final Set<String> BODY_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void afterCompletion(final @NonNull HttpServletRequest request,
        final @NonNull HttpServletResponse response,
        final @NonNull Object handler,
        final Exception ex) {
        try {
            final User actor = SecurityUtil.getLoggedInUser();
            if (actor == null) {
                return;
            }

            final String method = request.getMethod();
            eventPublisher.publishEvent(
                new AuditEvent(
                    actor.getId(),
                    method,
                    truncatePath(request.getRequestURI()),
                    response.getStatus(),
                    BODY_METHODS.contains(method) ? extractBody(request) : null,
                    DeviceInfoExtractor.extractIpAddress(request)
                )
            );
        } catch (final Exception ignored) {
            // No authenticated user or unexpected error — skip audit silently
        }
    }

    private String extractBody(final HttpServletRequest request) {
        final ContentCachingRequestWrapper wrapper = unwrapTo(request);
        if (wrapper == null) {
            return null;
        }
        final byte[] content = wrapper.getContentAsByteArray();
        return content.length > 0 ? new String(content, StandardCharsets.UTF_8) : null;
    }

    private ContentCachingRequestWrapper unwrapTo(final HttpServletRequest request) {
        HttpServletRequest current = request;
        while (current != null) {
            if (current instanceof final ContentCachingRequestWrapper ccw) {
                return ccw;
            }
            if (current instanceof final HttpServletRequestWrapper w) {
                current = (HttpServletRequest) w.getRequest();
            } else {
                break;
            }
        }
        return null;
    }

    private String truncatePath(final String path) {
        return path != null && path.length() > MAX_PATH_LENGTH ? path.substring(0, MAX_PATH_LENGTH) : path;
    }
}
