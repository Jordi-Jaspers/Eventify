package org.jordijaspers.eventify.api.event.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

@Data
@Entity
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "\"event\"")
public class Event implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @EmbeddedId
    private EventId id;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false
    )
    private Status status;

    @Column(
        name = "message",
        nullable = false
    )
    private String message;

    @Column(name = "correlation_id")
    private String correlationId;

    @CreationTimestamp
    @Column(
        name = "created",
        updatable = false
    )
    private LocalDateTime created;

}
