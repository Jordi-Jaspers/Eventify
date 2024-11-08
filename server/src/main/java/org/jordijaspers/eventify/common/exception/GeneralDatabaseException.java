package org.jordijaspers.eventify.common.exception;

import org.hawaiiframework.exception.ApiException;

import java.io.Serial;

import static org.jordijaspers.eventify.common.exception.ApiErrorCode.DATABASE_ERROR;

/**
 * An exception that is thrown when something goes wrong during a database operation.
 */
public class GeneralDatabaseException extends ApiException {

    @Serial
    private static final long serialVersionUID = -7873153884219235760L;

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
