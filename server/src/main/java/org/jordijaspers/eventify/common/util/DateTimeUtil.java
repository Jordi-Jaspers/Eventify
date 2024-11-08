package org.jordijaspers.eventify.common.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;

/**
 * Util for converting LocalDateTime objects between timezones.
 */
public final class DateTimeUtil {

    /**
     * Nanos in a second.
     */
    private static final double NANO_SECONDS_IN_A_SECOND = 1_000_000_000.0;

    /**
     * private constructor.
     */
    private DateTimeUtil() {
    }

    /**
     * Convert a localdatetime in a given timezone to localdatetime value for UTC.
     */
    public static LocalDateTime convertToUtcLocalDatetime(final LocalDateTime source, final ZoneId sourceTimeZone) {
        final ZonedDateTime zonedModifiedDate = ZonedDateTime.of(source, sourceTimeZone);
        final ZonedDateTime uctZdt = zonedModifiedDate.withZoneSameInstant(UTC);
        return uctZdt.toLocalDateTime();
    }

    /**
     * Converts a given {@link Duration} to a {@code double} containing the seconds and nanoseconds part.
     */
    public static double durationToSeconds(final Duration duration) {
        return (double) duration.getSeconds() + duration.getNano() / NANO_SECONDS_IN_A_SECOND;
    }
}
