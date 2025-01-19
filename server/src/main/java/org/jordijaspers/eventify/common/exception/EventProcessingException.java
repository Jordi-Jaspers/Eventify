package org.jordijaspers.eventify.common.exception;

import java.io.Serial;

import org.hawaiiframework.exception.ApiException;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.CANNOT_PROCESS_EVENT;

/**
 * Exception thrown when an event could not be processed.
 */
public class EventProcessingException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Constructor with the original exception.
     *
     * @param original The original exception.
     */
    public EventProcessingException(final Throwable original) {
        super(CANNOT_PROCESS_EVENT, original);
    }
}
