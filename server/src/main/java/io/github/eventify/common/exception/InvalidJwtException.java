package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.INVALID_TOKEN_ERROR;

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
