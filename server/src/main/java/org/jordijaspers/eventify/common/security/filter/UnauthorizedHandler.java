package org.jordijaspers.eventify.common.security.filter;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hawaiiframework.web.resource.ErrorResponseResource;
import org.jordijaspers.eventify.common.exception.AuthorizationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import static org.jordijaspers.eventify.common.exception.ApiErrorCode.UNAUTHORIZED_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Authentication entry point returning a 401 instead with customized exception message.
 */
@Slf4j
@Component
public final class UnauthorizedHandler implements AuthenticationEntryPoint {

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException)
        throws IOException {
        log.debug("Authentication failed: '{}'.", authException.getMessage());
        final ErrorResponseResource errorResponse = new ErrorResponseResource(new AuthorizationException(UNAUTHORIZED_ERROR));
        errorResponse.setErrorMessage(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        errorResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorResponse.setStatusMessage(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        errorResponse.setUri(request.getRequestURI());
        errorResponse.setQuery(request.getQueryString());
        errorResponse.setMethod(request.getMethod());
        errorResponse.setContentType(APPLICATION_JSON_VALUE);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"Restricted Content\"");
        response.setContentType(APPLICATION_JSON_VALUE);

        final ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        response.getWriter().write(writer.writeValueAsString(errorResponse));
    }
}
