package io.github.eventify.common.exception;

import io.github.jframe.exception.core.RateLimitExceededException;
import lombok.Getter;

import java.io.Serial;
import java.time.OffsetDateTime;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Exception thrown when a user has exceeded their monthly event quota.
 */
@Getter
public class QuotaExceededException extends RateLimitExceededException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Constructs a new QuotaExceededException with rate limit information.
     *
     * @param limit     the monthly quota limit
     * @param used      the number of events used
     * @param resetDate the date when quota resets
     */
    public QuotaExceededException(final int limit, final int used, final OffsetDateTime resetDate) {
        super(
            String.format("Monthly event quota exceeded: limit=%d, used=%d, resets at=%s", limit, used, resetDate),
            limit,
            Math.max(0, limit - used),
            resetDate
        );
    }
}
