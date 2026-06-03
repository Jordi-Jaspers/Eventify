package io.github.eventify.api.subscription.model;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Entity representing a user subscription to a watchlist for severity notifications.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "subscription")
public class Subscription implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "watchlist_id",
        nullable = false
    )
    private Watchlist watchlist;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "target_severities",
        nullable = false,
        columnDefinition = "jsonb"
    )
    private List<Severity> targetSeverities;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "adapters",
        nullable = false,
        columnDefinition = "jsonb"
    )
    private List<AdapterType> adapters;

    @CreationTimestamp
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
