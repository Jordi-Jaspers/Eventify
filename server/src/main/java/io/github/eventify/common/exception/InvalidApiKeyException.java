package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Exception thrown when API key is invalid or not found.
 */
public class InvalidApiKeyException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Constructor with error code.
     *
     * @param errorCode the error code
     */
    public InvalidApiKeyException(final ApiErrorCode errorCode) {
        super(errorCode);
    }
}
