package nl.vodafoneziggo.smc.cucc.common.exception;

import org.hawaiiframework.exception.ApiError;

import static java.util.Objects.requireNonNull;

/**
 * A general class for API error codes.
 */
public enum ApiErrorCode implements ApiError {

    INTERNAL_SERVER_ERROR(
        "ERR-0001",
        "Uncaught Exception: You think I know what went wrong here? If I did, I would've caught this exception no?"
    ),
    CANNOT_RETRIEVE_RESOURCE(
        "ERR-0002",
        "Something went wrong while retrieving the resource."
    ),
    DATABASE_ERROR(
        "ERR-0003",
        "Could not perform the requested action on the database. Contact administrator."
    );

    /**
     * The error code.
     */
    private final String errorCode;

    /**
     * The reason.
     */
    private final String reason;

    /**
     * Construct an instance with error code and reason.
     *
     * @param errorCode the error code
     * @param reason    the reason
     */
    ApiErrorCode(final String errorCode, final String reason) {
        this.errorCode = requireNonNull(errorCode);
        this.reason = requireNonNull(reason);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReason() {
        return reason;
    }
}
