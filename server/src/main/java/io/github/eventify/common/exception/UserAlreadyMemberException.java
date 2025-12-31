package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.USER_ALREADY_MEMBER_ERROR;

/**
 * Exception thrown when a user is already a member of an organization.
 */
public class UserAlreadyMemberException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public UserAlreadyMemberException() {
        super(USER_ALREADY_MEMBER_ERROR);
    }
}
