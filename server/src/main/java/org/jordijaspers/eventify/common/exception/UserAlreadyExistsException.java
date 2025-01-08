package org.jordijaspers.eventify.common.exception;

import java.io.Serial;

import org.hawaiiframework.exception.ApiException;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.USER_ALREADY_EXISTS_ERROR;

/**
 * Exception thrown when a user already exists.
 */
public class UserAlreadyExistsException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public UserAlreadyExistsException() {
        super(USER_ALREADY_EXISTS_ERROR);
    }
}
