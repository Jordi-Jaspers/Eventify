package org.jordijaspers.eventify.common.constants;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * A utility class containing constants used in controllers and services to know their endpoints.
 */
public final class DateTimeConstants {

    /**
     * The default timezone to use.
     */
    public static final String DEFAULT_TIMEZONE = "UTC";

    /**
     * The default timezone.
     */
    public static final ZoneId EUROPE_AMSTERDAM = ZoneId.of("Europe/Amsterdam");

    /**
     * The default date time formatter for {@link java.time.ZonedDateTime}.
     */
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Start of epoch.
     */
    public static final LocalDateTime START_OF_EPOCH = LocalDateTime.parse("1970-01-01T00:00:00");

    /* ------------------------------- END ------------------------------- */

    private DateTimeConstants() {
        // private constructor to prevent instantiation.
    }
}
