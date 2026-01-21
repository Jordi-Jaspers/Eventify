package io.github.eventify.api.event.model;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Entity representing an event in a channel. Stored in a TimescaleDB hypertable optimized for time-series data.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event")
public class Event implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "channel_id",
        nullable = false
    )
    private Channel channel;

    @Column(
        name = "severity",
        nullable = false
    )
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(
        name = "title",
        nullable = false
    )
    private String title;

    @Column(name = "message")
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "metadata",
        columnDefinition = "jsonb"
    )
    private Map<String, Object> metadata;

    @Column(
        name = "timestamp",
        nullable = false
    )
    private OffsetDateTime timestamp;

    /**
     * Constructs an Event from a CreateEventRequest and associated Channel.
     * Always uses server-generated timestamp (for single event ingestion).
     *
     * @param request the create event request
     * @param channel the associated channel
     */
    public Event(final CreateEventRequest request, final Channel channel) {
        this(request, channel, OffsetDateTime.now());
    }

    /**
     * Constructs an Event from a CreateEventRequest with explicit timestamp.
     * Used for batch ingestion where client provides the timestamp.
     *
     * @param request   the create event request
     * @param channel   the associated channel
     * @param timestamp the timestamp to use
     */
    public Event(final CreateEventRequest request, final Channel channel, final OffsetDateTime timestamp) {
        this.channel = channel;
        this.severity = request.getSeverity();
        this.title = request.getTitle();
        this.message = request.getMessage();
        this.metadata = request.getMetadata();
        this.timestamp = timestamp;
    }
}
