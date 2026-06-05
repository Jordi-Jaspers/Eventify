package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.ADAPTER_CONFIG_SYSTEM_MANAGED;

/**
 * Exception thrown when attempting to delete a system-managed adapter configuration.
 */
public class AdapterConfigSystemManagedException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public AdapterConfigSystemManagedException() {
        super(ADAPTER_CONFIG_SYSTEM_MANAGED);
    }
}
