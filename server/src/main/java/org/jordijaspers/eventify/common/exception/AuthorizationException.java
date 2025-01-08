package org.jordijaspers.eventify.common.exception;

import java.io.Serial;

import org.hawaiiframework.exception.ApiException;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

/**
 * An exception that indicates that the authentication failed, because of invalid credentials or because the user is not enabled/validated.
 */
public class AuthorizationException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor with an {@link ApiErrorCode}.
     */
    public AuthorizationException(final ApiErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructor with the original exception and an {@link ApiErrorCode}.
     */
    public AuthorizationException(final ApiErrorCode errorCode, final Throwable original) {
        super(errorCode, original);
    }
}
