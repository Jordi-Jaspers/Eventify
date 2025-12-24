package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.NON_EXISTING_USER_ERROR;

/**
 * Exception thrown when a user does not exist.
 */
public class NonExistingUserException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public NonExistingUserException() {
        super(NON_EXISTING_USER_ERROR);
    }
}
