package org.jordijaspers.eventify.common.exception;

import java.io.Serial;

import org.hawaiiframework.exception.ApiException;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.DATABASE_ERROR;

/**
 * An exception that is thrown when something goes wrong during a database operation.
 */
public class GeneralDatabaseException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     *
     * @param message  The message.
     * @param original The original exception.
     */
    public GeneralDatabaseException(final Throwable original, final String message) {
        super(DATABASE_ERROR, original, message);
    }

}
