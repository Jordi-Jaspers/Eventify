package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.CANNOT_ADD_DISABLED_USER_ERROR;

/**
 * Exception thrown when attempting to add a disabled user to an organization.
 */
public class DisabledUserException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public DisabledUserException() {
        super(CANNOT_ADD_DISABLED_USER_ERROR);
    }
}
