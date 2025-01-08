package org.jordijaspers.eventify.common.exception.handler;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import org.hawaiiframework.exception.ApiException;
import org.hawaiiframework.validation.ValidationException;
import org.hawaiiframework.web.exception.ErrorResponseEntityBuilder;
import org.hawaiiframework.web.exception.HttpException;
import org.hawaiiframework.web.resource.ApiErrorResponseResource;
import org.hawaiiframework.web.resource.ErrorResponseResource;
import org.hawaiiframework.web.resource.MethodArgumentNotValidResponseResource;
import org.hawaiiframework.web.resource.ValidationErrorResponseResource;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.common.exception.GeneralDatabaseException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.springframework.http.HttpHeaders.EMPTY;
import static org.springframework.http.HttpStatus.*;

/**
 * This class creates proper HTTP response bodies for exceptions.
 *
 * @author Jordi Jaspers
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ResponseEntityExceptionHandler {

    private final ErrorResponseEntityBuilder errorResponseEntityBuilder;

    /**
     * Constructor.
     *
     * @param errorResponseEntityBuilder the error response entity builder
     */
    public ResponseEntityExceptionHandler(final ErrorResponseEntityBuilder errorResponseEntityBuilder) {
        this.errorResponseEntityBuilder = errorResponseEntityBuilder;
    }

    /**
     * Handles {@code HttpException} instances.
     *
     * <p>Each {@code HttpException} has an associated {@code HttpStatus} that is used as the response
     * status.
     *
     * @param exception the exception
     * @param request   the current request
     * @return a response entity reflecting the current exception
     */
    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(HttpException.class)
    @ApiResponse(
        responseCode = "400 (default)",
        description = "Default HTTP Exception",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiErrorResponseResource.class)
        )
    )
    public ResponseEntity<Object> handleHttpException(HttpException exception, WebRequest request) {
        final HttpStatus status = exception.getHttpStatus();
        return new ResponseEntity<>(buildErrorResponseBody(exception, status, request), EMPTY, status);
    }

    /**
     * Handles {@code ApiException} instances.
     *
     * <p>The response status is: 400 Bad Request.
     *
     * @param exception the exception
     * @param request   the current request
     * @return a response entity reflecting the current exception
     */
    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ApiException.class)
    @ApiResponse(
        responseCode = "400 (API)",
        description = "API Exception",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiErrorResponseResource.class)
        )
    )
    public ResponseEntity<Object> handleApiException(final ApiException exception, final WebRequest request) {
        return new ResponseEntity<>(buildErrorResponseBody(exception, BAD_REQUEST, request), EMPTY, BAD_REQUEST);
    }

    /**
     * Handles {@code MethodArgumentNotValidException} instances.
     *
     * @param exception the exception
     * @param request   the current request
     * @return a response entity reflecting the current exception
     */
    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponse(
        responseCode = "400 (Validation)",
        description = "Input Validation Exception",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = MethodArgumentNotValidResponseResource.class)
        )
    )
    public ResponseEntity<Object> handleValidationException(final MethodArgumentNotValidException exception, final WebRequest request) {
        return new ResponseEntity<>(buildErrorResponseBody(exception, BAD_REQUEST, request), EMPTY, BAD_REQUEST);
    }

    /**
     * Handles {@code ValidationException} instances.
     *
     * <p>The response status is: 400 Bad Request.
     *
     * @param exception the exception
     * @param request   the current request
     * @return a response entity reflecting the current exception
     */
    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    @ApiResponse(
        responseCode = "400 (Validation)",
        description = "Input Validation Exception",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ValidationErrorResponseResource.class)
        )
    )
    public ResponseEntity<Object> handleValidationException(
        ValidationException exception, WebRequest request) {
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(buildErrorResponseBody(exception, status, request), EMPTY, status);
    }

    /**
     * Handles {@code AccessDeniedException} errors.
     * <p>
     *
     * @param exception the exception
     * @param request   the current request
     * @return a response entity reflecting the current exception
     */
    @ResponseBody
    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    @ApiResponse(
        responseCode = "403",
        description = "Access Denied",
        content = @Content(
            schema = @Schema(implementation = ErrorResponseResource.class)
        )
    )
    public ResponseEntity<?> handleAccessDeniedException(final AccessDeniedException exception, final WebRequest request) {
        final HttpStatus status = FORBIDDEN;
        return ResponseEntity.status(status).body(buildErrorResponseBody(exception, status, request));
    }

    /**
     * Handles {@code NoResourceFoundException} errors.
     *
     * @param exception the exception
     * @param request   the current request
     * @return a response entity reflecting the current exception
     */
    @ResponseBody
    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    @ApiResponse(
        responseCode = "404",
        description = "Resource Not Found",
        content = @Content(
            schema = @Schema(implementation = ErrorResponseResource.class)
        )
    )
    public ResponseEntity<?> handleNoResourceFoundException(final NoResourceFoundException exception, final WebRequest request) {
        final HttpStatus status = NOT_FOUND;
        return ResponseEntity.status(status).body(buildErrorResponseBody(exception, status, request));
    }

    /**
     * Handles {@code Throwable} instances. This method acts as a fallback handler.
     *
     * @param throwable the exception
     * @param request   the current request
     * @return a response entity reflecting the current exception
     */
    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    @ApiResponse(
        responseCode = "500",
        description = "Uncaught Exceptions - Internal Server Error",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponseResource.class)
        )
    )
    public ResponseEntity<Object> handleThrowable(final Throwable throwable, final WebRequest request) {
        log.error(ApiErrorCode.INTERNAL_SERVER_ERROR.getReason(), throwable);
        return new ResponseEntity<>(buildErrorResponseBody(throwable, INTERNAL_SERVER_ERROR, request), EMPTY, INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@code DataAccessException} instances.
     *
     * <p>The response status is: 400 Bad Request.
     *
     * @param exception the exception
     * @param request   the current request
     * @return a response entity reflecting the current exception
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDatabaseExceptions(final DataAccessException exception, final WebRequest request) {
        log.error("Something went wrong during a database operation", exception);
        return handleApiException(new GeneralDatabaseException(exception, "Cannot execute database operation."), request);
    }

    private ErrorResponseResource buildErrorResponseBody(final Throwable throwable,
        final HttpStatus status,
        final WebRequest request) {
        return errorResponseEntityBuilder.buildErrorResponseBody(throwable, status, request);
    }
}
