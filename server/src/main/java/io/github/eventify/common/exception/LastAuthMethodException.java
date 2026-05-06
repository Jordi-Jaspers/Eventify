package io.github.eventify.common.exception;

import io.github.jframe.exception.HttpException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.LAST_AUTH_METHOD_ERROR;

/**
 * Exception thrown when attempting to unlink the last authentication method.
 */
public class LastAuthMethodException extends HttpException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public LastAuthMethodException() {
        super(LAST_AUTH_METHOD_ERROR.getReason(), jakarta.ws.rs.core.Response.Status.CONFLICT);
    }

    /**
     * Constructor with custom message (for subclasses).
     *
     * @param message the error message
     */
    protected LastAuthMethodException(final String message) {
        super(message, jakarta.ws.rs.core.Response.Status.CONFLICT);
    }
}
