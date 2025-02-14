package org.jordijaspers.eventify.api.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.Embeddable;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

/**
 * The EventId class represents the primary key of the Event entity.
 */
@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class EventId implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private Long checkId;

    private LocalDateTime timestamp;

}
