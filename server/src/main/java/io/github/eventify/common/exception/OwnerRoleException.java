package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Exception thrown when attempting invalid operations on owner role.
 */
public class OwnerRoleException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Constructor with specific error code.
     *
     * @param errorCode the specific owner role error code
     */
    public OwnerRoleException(final ApiErrorCode errorCode) {
        super(errorCode);
    }
}
