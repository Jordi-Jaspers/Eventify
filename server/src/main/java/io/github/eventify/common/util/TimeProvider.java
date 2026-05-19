package io.github.eventify.common.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for providing timestamps with consistent precision.
 *
 * <p>PostgreSQL TIMESTAMPTZ has microsecond precision (6 decimal places),
 * while Java OffsetDateTime.now() has nanosecond precision (9 decimal places).
 * This utility ensures all timestamps are truncated to microsecond precision
 * to prevent precision mismatches when storing and retrieving from the database.
 */
@UtilityClass
public class TimeProvider {

    /**
     * Returns the current timestamp truncated to microsecond precision.
     *
     * <p>This matches PostgreSQL's TIMESTAMPTZ precision and ensures
     * consistent behavior when comparing timestamps before and after
     * database operations.
     *
     * @return the current timestamp with microsecond precision
     */
    public static OffsetDateTime now() {
        return OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }

    /**
     * Truncates a timestamp to microsecond precision.
     *
     * <p>Use this when you have an existing timestamp that needs
     * to be normalized to database precision.
     *
     * @param timestamp the timestamp to truncate
     * @return the truncated timestamp, or null if input is null
     */
    public static OffsetDateTime truncateToMicros(final OffsetDateTime timestamp) {
        return timestamp == null ? null : timestamp.truncatedTo(ChronoUnit.MICROS);
    }

    /**
     * Returns the start of the given date at UTC midnight as an OffsetDateTime.
     *
     * @param date the local date
     * @return the start of day in UTC
     */
    public static OffsetDateTime startOfDayUtc(final LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
    }
}
