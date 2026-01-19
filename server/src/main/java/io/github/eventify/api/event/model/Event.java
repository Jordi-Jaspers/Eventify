package io.github.eventify.api.event.model;

import io.github.eventify.api.channel.model.Channel;
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
 * Entity representing an event in a channel.
 * Stored in a TimescaleDB hypertable optimized for time-series data.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
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
     * Creates a new event with the specified details.
     *
     * @param channel   the channel this event belongs to
     * @param severity  the severity level of the event
     * @param title     the event title
     * @param message   the event message (optional)
     * @param metadata  additional metadata as key-value pairs (optional)
     * @param timestamp the timestamp when the event occurred
     */
    public Event(
                 final Channel channel,
                 final Severity severity,
                 final String title,
                 final String message,
                 final Map<String, Object> metadata,
                 final OffsetDateTime timestamp
    ) {
        this.channel = channel;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.metadata = metadata;
        this.timestamp = timestamp;
    }
}
