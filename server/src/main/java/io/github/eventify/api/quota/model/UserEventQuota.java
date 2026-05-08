package io.github.eventify.api.quota.model;

import io.github.eventify.api.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import jakarta.persistence.*;

import org.hibernate.annotations.UpdateTimestamp;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static java.time.ZoneOffset.UTC;

/**
 * Entity representing user event quota tracking.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "user_event_quota")
public class UserEventQuota implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true
    )
    private User user;

    @Column(
        name = "event_count",
        nullable = false
    )
    private Integer eventCount = 0;

    @Column(
        name = "period_start",
        nullable = false
    )
    private OffsetDateTime periodStart;

    @UpdateTimestamp
    @Column(
        name = "updated_at",
        nullable = false
    )
    private OffsetDateTime updatedAt;

    /**
     * Constructor to create a new UserEventQuota for a user.
     *
     * @param user the user
     */
    public UserEventQuota(final User user) {
        this.user = user;
        this.updatedAt = OffsetDateTime.now(UTC);
        this.periodStart = OffsetDateTime.now(UTC)
            .withDayOfMonth(1)
            .with(LocalTime.MIN);
    }
}
