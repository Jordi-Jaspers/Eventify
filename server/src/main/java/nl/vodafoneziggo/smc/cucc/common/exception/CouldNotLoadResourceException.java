package nl.vodafoneziggo.smc.cucc.common.exception;

import org.hawaiiframework.exception.ApiException;

import java.io.Serial;

import static nl.vodafoneziggo.smc.cucc.common.exception.ApiErrorCode.CANNOT_RETRIEVE_RESOURCE;


/**
 * Exception thrown when something went wrong retrieving resources.
 */
public class CouldNotLoadResourceException extends ApiException {

    /**
     * The serialVersionUID.
     */
    @Serial
    private static final long serialVersionUID = 6478379730702994868L;

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
