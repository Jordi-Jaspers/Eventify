package io.github.eventify.api.channel.model;

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
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "channel")
public class Channel implements PageableItem, Serializable {

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

    /**
     * Creates a new channel with the specified name, user, and organization.
     *
     * @param name         the channel name
     * @param user         the user who owns (personal) or created (org) this channel
     * @param organization the organization this channel belongs to, or null for personal channels
     */
    public Channel(final String name, final User user, final Organization organization) {
        this.name = name;
        this.user = user;
        this.organization = organization;
        this.status = ChannelStatus.ACTIVE;
    }
}
