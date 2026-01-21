package io.github.eventify.common.exception;

import io.github.jframe.exception.HttpException;
import lombok.Getter;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.exception.ApiErrorCode.QUOTA_EXCEEDED;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

/**
 * Exception thrown when a user has exceeded their monthly event quota.
 */
@Getter
public class QuotaExceededException extends HttpException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Constructs a new QuotaExceededException with the specified message.
     */
    public QuotaExceededException() {
        super(QUOTA_EXCEEDED.getReason(), TOO_MANY_REQUESTS);
    }
}
