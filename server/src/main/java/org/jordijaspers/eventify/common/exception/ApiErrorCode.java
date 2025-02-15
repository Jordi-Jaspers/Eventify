package org.jordijaspers.eventify.common.exception;

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
    ),
    TOKEN_NOT_FOUND_ERROR(
        "ERR-0004",
        "Provided token does not exist, double check the token."
    ),
    INVALID_TOKEN_ERROR(
        "ERR-0005",
        "The provided JWT token is invalid."
    ),
    INVALID_CREDENTIALS(
        "ERR-0006",
        "Invalid Credentials: Username or password is incorrect."
    ),
    USER_LOCKED_ERROR(
        "ERR-0007",
        "Authorization failed: your account has been locked by the admin."
    ),
    USER_UNVALIDATED_ERROR(
        "ERR-0008",
        "Authorization failed: The user must validate their email address first."
    ),
    PASSWORD_DOES_NOT_MATCH(
        "ERR-0009",
        "The provided password does not match the current password."
    ),
    USER_ALREADY_EXISTS_ERROR(
        "ERR-0010",
        "User cannot be created: The provided email address is already in use."
    ),
    DASHBOARD_NOT_FOUND_ERROR(
        "ERR-0011",
        "The requested dashboard does not exist."
    ),
    TEAM_NOT_FOUND_ERROR(
        "ERR-0012",
        "The requested team does not exist."
    ),
    USER_NOT_FOUND_ERROR(
        "ERR-0013",
        "One or more users could not be found."
    ),
    TEAM_ALREADY_EXISTS_ERROR(
        "ERR-0014",
        "A team with that name already exists."
    ),
    USER_NOT_LOGGED_IN(
        "ERR-0015",
        "No user is logged in. Cannot perform action without authorization."
    ),
    USER_NOT_PART_OF_TEAM(
        "ERR-0016",
        "The user is not part of the specified team, and therefore cannot perform this action."
    ),
    CANNOT_ACCESS_DASHBOARD(
        "ERR-0017",
        "The user does not have access to the specified dashboard."
    ),
    UNAUTHORIZED_ERROR(
        "ERR-0018",
        "You are not authorized to access this content."
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
