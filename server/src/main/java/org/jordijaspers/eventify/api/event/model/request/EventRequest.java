package org.jordijaspers.eventify.api.event.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

import org.jordijaspers.eventify.api.event.model.Status;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

/**
 * The EventRequest class represents the request payload for creating an event.
 */
@Data
@NoArgsConstructor
public class EventRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private Long checkId;

    private ZonedDateTime timestamp;

    private Status status;

    private String message;

    private String correlationId;

}
