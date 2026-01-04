package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.OAUTH2_AUTHENTICATION_ERROR;

/**
 * Exception thrown when OAuth2 authentication processing fails.
 */
public class OAuth2Exception extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Constructor with a custom error message.
     *
     * @param message The error message.
     */
    public OAuth2Exception(final String message) {
        super(OAUTH2_AUTHENTICATION_ERROR, message);
    }

    /**
     * Constructor with a custom error message and original cause.
     *
     * @param message The error message.
     * @param cause   The original cause of the exception.
     */
    public OAuth2Exception(final String message, final Throwable cause) {
        super(OAUTH2_AUTHENTICATION_ERROR, cause, message);
    }
}
