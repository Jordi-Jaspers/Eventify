package io.github.eventify.common.security.filter;

import io.github.jframe.exception.resource.ErrorResponseResource;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The unauthorized handler.
 */
@Slf4j
public final class UnauthorizedHandler {

    private UnauthorizedHandler() {
        // No-args constructor to prevent instantiation.
    }

    /**
     * Handle the authentication failure.
     *
     * @param request  the request
     * @param response the response
     * @param message  the message
     */
    public static void handleAuthenticationFailure(final HttpServletRequest request,
        final HttpServletResponse response,
        final String message) {
        try {
            final HttpStatus status = HttpStatus.UNAUTHORIZED;
            final BadCredentialsException exception = new BadCredentialsException(message);

            final ErrorResponseResource errorResponseResource = new ErrorResponseResource(exception);
            errorResponseResource.setErrorMessage(message);
            errorResponseResource.setStatusCode(status.value());
            errorResponseResource.setStatusMessage(status.getReasonPhrase());
            errorResponseResource.setMethod(request.getMethod());
            errorResponseResource.setUri(request.getRequestURI());
            errorResponseResource.setContentType(APPLICATION_JSON_VALUE);

            final ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            response.getWriter().write(writer.writeValueAsString(errorResponseResource));
            response.setStatus(errorResponseResource.getStatusCode());
            response.setContentType(errorResponseResource.getContentType());
        } catch (final Exception exception) {
            log.error("Failed to write the error response: {}", exception.getMessage());
        }
    }
}
