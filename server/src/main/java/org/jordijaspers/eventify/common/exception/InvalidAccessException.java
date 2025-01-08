package org.jordijaspers.eventify.common.exception;

import java.io.Serial;

import org.hawaiiframework.exception.ApiException;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

/**
 * Exception thrown when a user tries to access a resource that is not part of a certain team.
 */
public class InvalidAccessException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public InvalidAccessException(final ApiErrorCode apiErrorCode) {
        super(apiErrorCode);
    }
}
