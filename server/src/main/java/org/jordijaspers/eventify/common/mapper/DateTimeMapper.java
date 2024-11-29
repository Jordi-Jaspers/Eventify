package org.jordijaspers.eventify.common.mapper;

import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.isNull;

/**
 * Mapper that converts a local date time to a zoned date time.
 */
@Mapper(config = SharedMapperConfig.class)
public class DateTimeMapper {

    /**
     * Converts a LocalDateTime to a ZonedDateTime, assuming zone UTC.
     */
    public ZonedDateTime oUTCZonedDateTimeFromLocal(final LocalDateTime localDateTime) {
        return isNull(localDateTime)
            ? null
            : ZonedDateTime.of(localDateTime, UTC);
    }

    /**
     * Converts an OffsetDateTime to a ZonedDateTime.
     */
    public ZonedDateTime toZonedDateTimeFromOffset(final OffsetDateTime offsetDateTime) {
        return isNull(offsetDateTime)
            ? null
            : offsetDateTime.toZonedDateTime();
    }

    /**
     * Converts an Instant to a ZonedDateTime, assuming zone UTC.
     */
    public ZonedDateTime toUTCZonedDateTimeFromInstant(final Instant instant) {
        return isNull(instant)
            ? null
            : ZonedDateTime.ofInstant(instant, UTC);
    }

    /**
     * Converts a ZonedDateTime to a LocalDateTime.
     */
    public LocalDateTime toLocalDateTimeFromZone(final ZonedDateTime zonedDateTime) {
        return isNull(zonedDateTime)
            ? null
            : zonedDateTime.toLocalDateTime();
    }

    /**
     * Converts a ZonedDateTime to an Instant.
     */
    public Instant oInstantFromZone(final ZonedDateTime zonedDateTime) {
        return isNull(zonedDateTime)
            ? null
            : zonedDateTime.toInstant();
    }

    /**
     * Converts a ZonedDateTime to an OffsetDateTime.
     */
    public OffsetDateTime toOffsetDateTimeFromZone(final ZonedDateTime zonedDateTime) {
        return isNull(zonedDateTime)
            ? null
            : zonedDateTime.toOffsetDateTime();
    }
}
