package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.CANNOT_DEMOTE_LAST_ADMIN_ERROR;

/**
 * Exception thrown when a user tries to demote the last admin.
 */
public class DemoteLastAdminException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public DemoteLastAdminException() {
        super(CANNOT_DEMOTE_LAST_ADMIN_ERROR);
    }
}
