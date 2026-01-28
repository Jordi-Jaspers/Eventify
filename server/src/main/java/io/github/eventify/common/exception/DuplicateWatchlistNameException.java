package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.DUPLICATE_WATCHLIST_NAME;

/**
 * Exception thrown when a watchlist with the same name already exists.
 */
public class DuplicateWatchlistNameException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Creates a new DuplicateWatchlistNameException.
     */
    public DuplicateWatchlistNameException() {
        super(DUPLICATE_WATCHLIST_NAME);
    }
}
