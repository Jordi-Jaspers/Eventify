package org.jordijaspers.eventify.common.mapper;

import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Mapper that converts a local date time to a zoned date time.
 */
@Mapper(config = SharedMapperConfig.class)
public class DateTimeMapper {

    /**
     * Converts a LocalDateTime to a ZonedDateTime, assuming zone UTC.
     */
    public ZonedDateTime toUTCZonedDateTime(final LocalDateTime localDateTime) {
        if (isNull(localDateTime)) {
            return null;
        }
        return ZonedDateTime.of(localDateTime, UTC);
    }

    /**
     * Converts a list of LocalDateTime to a list of ZonedDateTime, assuming zone UTC.
     */
    public List<ZonedDateTime> toUTCZonedDateTimeList(final List<LocalDateTime> localDateTimes) {
        if (isEmpty(localDateTimes)) {
            return new ArrayList<>();
        }
        return localDateTimes.stream().map(local -> ZonedDateTime.of(local, UTC)).toList();
    }
}
