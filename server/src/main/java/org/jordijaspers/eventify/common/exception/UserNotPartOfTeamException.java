package org.jordijaspers.eventify.common.exception;

import org.hawaiiframework.exception.ApiException;

import java.io.Serial;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.USER_NOT_PART_OF_TEAM;

/**
 * Exception thrown when a user is not part of a certain team.
 */
public class UserNotPartOfTeamException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public UserNotPartOfTeamException() {
        super(USER_NOT_PART_OF_TEAM);
    }
}
