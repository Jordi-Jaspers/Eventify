package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.CHANNEL_ACCESS_DENIED;

/**
 * Exception thrown when access to a channel is denied.
 * Returns HTTP 403 FORBIDDEN.
 */
public class ChannelAccessDeniedException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public ChannelAccessDeniedException() {
        super(CHANNEL_ACCESS_DENIED);
    }
}
