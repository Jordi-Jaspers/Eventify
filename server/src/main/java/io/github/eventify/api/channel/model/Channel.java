package io.github.eventify.api.channel.model;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.TimelineSource;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.jframe.datasource.search.model.PageableItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Entity representing a channel for event streaming.
 * Implements {@link TimelineSource} to participate in timeline consolidation.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "channel")
public class Channel implements PageableItem, Serializable, TimelineSource {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "name",
        nullable = false,
        length = 100
    )
    private String name;

    @Column(
        name = "slug",
        nullable = false,
        length = 100
    )
    private String slug;

    @Column(
        name = "description",
        length = 500
    )
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(
        name = "status",
        nullable = false
    )
    @Enumerated(EnumType.STRING)
    private ChannelStatus status;

    @CreationTimestamp
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "last_event_at")
    private OffsetDateTime lastEventAt;

    @Column(
        name = "is_stale",
        nullable = false
    )
    private Boolean isStale;

    @Transient
    private Timeline timeline;

    @Transient
    private Severity currentSeverity;

    /**
     * Creates a new channel with the specified name, slug, user, and organization.
     *
     * @param name         the channel name
     * @param slug         the channel slug identifier
     * @param user         the user who owns (personal) or created (org) this channel
     * @param organization the organization this channel belongs to, or null for personal channels
     */
    public Channel(final String name, final String slug, final User user, final Organization organization) {
        this.name = name;
        this.slug = slug;
        this.user = user;
        this.organization = organization;
        this.status = ChannelStatus.ACTIVE;
        this.isStale = false;
    }

    /**
     * Creates a Channel with only the id populated.
     */
    public Channel(final Long id) {
        this.id = id;
    }
}
