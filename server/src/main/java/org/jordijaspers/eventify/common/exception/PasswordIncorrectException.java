package org.jordijaspers.eventify.common.exception;

import java.io.Serial;

import org.hawaiiframework.exception.ApiException;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.PASSWORD_DOES_NOT_MATCH;

/**
 * Exception thrown when the password does not match the current password.
 */
public class PasswordIncorrectException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public PasswordIncorrectException() {
        super(PASSWORD_DOES_NOT_MATCH);
    }
}
