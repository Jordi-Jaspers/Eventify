package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.CHANNEL_PAUSED;

/**
 * Exception thrown when attempting to access a paused channel.
 */
public class ChannelPausedException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor with message "Channel is paused".
     */
    public ChannelPausedException() {
        super(CHANNEL_PAUSED);
    }
}
