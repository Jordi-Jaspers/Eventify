package org.jordijaspers.eventify.common.exception;

import java.io.Serial;

import org.hawaiiframework.exception.ApiException;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.INVALID_TOKEN_ERROR;


/**
 * Exception thrown when parsing an invalid JWT.
 */
public class InvalidJwtException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public InvalidJwtException() {
        super(INVALID_TOKEN_ERROR);
    }

    /**
     * Default constructor with the original exception.
     */
    public InvalidJwtException(final Exception original) {
        super(INVALID_TOKEN_ERROR, original);
    }
}
