package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.API_KEY_INVALID_EXPIRATION;

/**
 * Exception thrown when API key expiration date is invalid.
 */
public class ApiKeyInvalidExpirationException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public ApiKeyInvalidExpirationException() {
        super(API_KEY_INVALID_EXPIRATION);
    }
}
