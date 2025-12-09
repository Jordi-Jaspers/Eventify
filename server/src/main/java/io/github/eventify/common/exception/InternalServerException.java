package io.github.eventify.common.exception;


import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.INTERNAL_SERVER_ERROR;


/**
 * Placeholder exception for any uncaught exception within the application.
 */
public class InternalServerException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public InternalServerException() {
        super(INTERNAL_SERVER_ERROR);
    }

    /**
     * Constructor with the original exception.
     *
     * @param original The original exception.
     */
    public InternalServerException(final Throwable original) {
        super(INTERNAL_SERVER_ERROR, original);
    }
}
