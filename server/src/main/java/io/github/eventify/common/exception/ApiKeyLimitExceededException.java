package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.API_KEY_LIMIT_EXCEEDED;

/**
 * Exception thrown when user has reached the maximum number of API keys.
 */
public class ApiKeyLimitExceededException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public ApiKeyLimitExceededException() {
        super(API_KEY_LIMIT_EXCEEDED);
    }
}
