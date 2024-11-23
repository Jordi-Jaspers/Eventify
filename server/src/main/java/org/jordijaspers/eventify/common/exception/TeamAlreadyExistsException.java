package org.jordijaspers.eventify.common.exception;

import org.hawaiiframework.exception.ApiException;

import java.io.Serial;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.TEAM_ALREADY_EXISTS_ERROR;

/**
 * Exception thrown when trying to create a team that already exists.
 */
public class TeamAlreadyExistsException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public TeamAlreadyExistsException() {
        super(TEAM_ALREADY_EXISTS_ERROR);
    }
}
