package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.DUPLICATE_CHANNEL_SLUG;

/**
 * Exception thrown when a channel slug is already in use for the owner.
 */
public class DuplicateChannelSlugException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public DuplicateChannelSlugException() {
        super(DUPLICATE_CHANNEL_SLUG);
    }
}
