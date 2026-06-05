package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.ORGANIZATION_SUSPENDED_ERROR;

/**
 * Exception thrown when an action is attempted on or involving a suspended organization.
 */
public class OrganizationSuspendedException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Creates a new OrganizationSuspendedException.
     */
    public OrganizationSuspendedException() {
        super(ORGANIZATION_SUSPENDED_ERROR);
    }

    @Override
    public String getMessage() {
        return getApiError().getReason();
    }
}
