package org.jordijaspers.eventify.common.exception;

import java.io.Serial;

import org.hawaiiframework.exception.ApiException;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.CANNOT_RETRIEVE_RESOURCE;


/**
 * Exception thrown when something went wrong retrieving resources.
 */
public class CouldNotLoadResourceException extends ApiException {

    /**
     * The serialVersionUID.
     */
    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Constructor.
     */
    public CouldNotLoadResourceException(final Exception original) {
        super(CANNOT_RETRIEVE_RESOURCE, original);
    }

    /**
     * Constructor.
     */
    public CouldNotLoadResourceException(final String message) {
        super(CANNOT_RETRIEVE_RESOURCE, message);
    }
}
