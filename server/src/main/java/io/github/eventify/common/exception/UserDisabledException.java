package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Exception thrown when user account is disabled.
 */
public class UserDisabledException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Constructor with error code.
     *
     * @param errorCode the error code
     */
    public UserDisabledException(final ApiErrorCode errorCode) {
        super(errorCode);
    }

    @Override
    public String getMessage() {
        return getApiError().getReason();
    }
}
