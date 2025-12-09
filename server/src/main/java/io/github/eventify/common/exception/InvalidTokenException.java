package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Exception thrown when an invalid token is encountered.
 */
public class InvalidTokenException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public InvalidTokenException(final ApiErrorCode errorCode) {
        super(errorCode);
    }
}
