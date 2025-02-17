package org.jordijaspers.eventify.api.event.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import jakarta.persistence.Embeddable;

import static java.time.ZoneOffset.UTC;
import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

/**
 * The EventId class represents the primary key of the Event entity.
 */
@Data
@Embeddable
@NoArgsConstructor
public class EventId implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private Long checkId;

    private OffsetDateTime timestamp;

    /**
     * Creates a new EventId instance with the provided check id and timestamp in their respective zone.
     *
     * @param checkId   the check id
     * @param timestamp the timestamp in a certain zone.
     */
    public EventId(final Long checkId, final ZonedDateTime timestamp) {
        this.checkId = checkId;
        this.timestamp = timestamp.withZoneSameInstant(UTC).toOffsetDateTime();
    }

    /**
     * Creates a new EventId instance with the provided check id and timestamp.
     *
     * @param checkId   the check id
     * @param timestamp the timestamp
     */
    public EventId(final Long checkId, final OffsetDateTime timestamp) {
        this.checkId = checkId;
        this.timestamp = timestamp;
    }
}
