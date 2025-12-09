package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.USER_ALREADY_EXISTS_ERROR;

/**
 * Exception thrown when a user already exists.
 */
public class UserAlreadyExistsException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public UserAlreadyExistsException() {
        super(USER_ALREADY_EXISTS_ERROR);
    }
}
