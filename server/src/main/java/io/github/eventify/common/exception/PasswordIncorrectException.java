package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.PASSWORD_DOES_NOT_MATCH;

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
