package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.CANNOT_LOCK_SELF_ERROR;

/**
 * Exception thrown when a user tries to lock themselves.
 */
public class SelfLockingException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public SelfLockingException() {
        super(CANNOT_LOCK_SELF_ERROR);
    }
}
