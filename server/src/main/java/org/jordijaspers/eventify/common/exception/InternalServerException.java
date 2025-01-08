package org.jordijaspers.eventify.common.exception;

import java.io.Serial;

import org.hawaiiframework.exception.ApiException;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.INTERNAL_SERVER_ERROR;

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
