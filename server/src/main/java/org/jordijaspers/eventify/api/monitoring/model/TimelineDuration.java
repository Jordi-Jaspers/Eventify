package org.jordijaspers.eventify.api.monitoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import jakarta.persistence.*;

import org.jordijaspers.eventify.api.event.model.Status;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;


@Data
@Entity
@NoArgsConstructor
public class TimelineDuration implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @EmbeddedId
    private TimelineDurationId id;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "end_time")
    private ZonedDateTime endTime;

    /**
     * Since we're using native query, we need a constructor that matches the query result.
     *
     * @param checkId   The check ID
     * @param status    The status
     * @param startTime The start time
     * @param endTime   The end time
     */
    public TimelineDuration(final Long checkId, final String status, final ZonedDateTime startTime, final ZonedDateTime endTime) {
        this.id = new TimelineDurationId(checkId, startTime);
        this.status = Status.valueOf(status);
        this.endTime = endTime;
    }
}
