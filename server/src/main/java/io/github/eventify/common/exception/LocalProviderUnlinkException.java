package io.github.eventify.common.exception;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.LOCAL_PROVIDER_UNLINK_ERROR;

/**
 * Exception thrown when attempting to unlink the LOCAL (password) authentication provider.
 * Extends LastAuthMethodException so existing last-auth-method checks remain compatible.
 */
public class LocalProviderUnlinkException extends LastAuthMethodException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public LocalProviderUnlinkException() {
        super(LOCAL_PROVIDER_UNLINK_ERROR.getReason());
    }
}
