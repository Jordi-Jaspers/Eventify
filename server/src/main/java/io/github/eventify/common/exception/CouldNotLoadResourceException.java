package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.COULD_NOT_LOAD_RESOURCE;

/** Exception thrown when a resource cannot be loaded from the classpath. */
public class CouldNotLoadResourceException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Constructor with message.
     *
     * @param message the error message
     */
    public CouldNotLoadResourceException(final String message) {
        super(COULD_NOT_LOAD_RESOURCE, message);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public CouldNotLoadResourceException(final String message, final Throwable cause) {
        super(COULD_NOT_LOAD_RESOURCE, cause, message);
    }
}
