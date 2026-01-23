package io.github.eventify.api.watchlist.model;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Entity representing a watchlist for event timeline monitoring.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "watchlist")
public class Watchlist implements PageableItem, Serializable {

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "configuration",
        nullable = false,
        columnDefinition = "jsonb"
    )
    private WatchlistConfiguration configuration;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "filters",
        nullable = false,
        columnDefinition = "jsonb"
    )
    private WatchlistFilters filters;

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
     * Creates a new watchlist with the specified name, user, and organization.
     *
     * @param name         the watchlist name
     * @param user         the user who owns (personal) or created (org) this watchlist
     * @param organization the organization this watchlist belongs to, or null for personal watchlists
     */
    public Watchlist(final String name, final User user, final Organization organization) {
        this.name = name;
        this.user = user;
        this.organization = organization;
        this.configuration = WatchlistConfiguration.empty();
        this.filters = WatchlistFilters.defaults();
    }
}
