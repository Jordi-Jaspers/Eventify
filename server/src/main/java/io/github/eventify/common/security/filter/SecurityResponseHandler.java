package io.github.eventify.common.security.filter;

import io.github.jframe.exception.resource.ErrorResponseResource;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Utility class for writing security error responses.
 */
@Slf4j
@UtilityClass
public final class SecurityResponseHandler {

    /**
     * Handles authentication failure by writing a 401 response.
     */
    public static void handleUnauthorizedAccess(final HttpServletRequest req, final HttpServletResponse res, final String message) {
        writeErrorResponse(req, res, HttpStatus.UNAUTHORIZED, new BadCredentialsException(message), message);
    }

    /**
     * Handles forbidden access by writing a 403 response.
     */
    public static void handleForbiddenAccess(final HttpServletRequest req, final HttpServletResponse res, final String message) {
        writeErrorResponse(req, res, HttpStatus.FORBIDDEN, new AccessDeniedException(message), message);
    }

    private void writeErrorResponse(final HttpServletRequest request,
        final HttpServletResponse response,
        final HttpStatus status,
        final Exception exception,
        final String message) {
        try {
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
        } catch (final Exception ex) {
            log.error("Failed to write the error response: {}", ex.getMessage());
        }
    }
}
