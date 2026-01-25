package io.github.eventify.api.watchlist.model;

import io.github.eventify.api.monitor.model.MonitorFilters;
import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.monitor.model.TimeSpan;
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

    /**
     * Resolves the effective time range context from request filters.
     * Falls back to this watchlist's default timeRange if not specified in request.
     *
     * @param requestFilters the request filters (may be null)
     * @return the resolved time span
     */
    public TimeSpan resolveTimeRange(final MonitorFilters requestFilters) {
        if (requestFilters != null && requestFilters.getStartTime() != null && requestFilters.getEndTime() != null) {
            return new TimeSpan(requestFilters.getStartTime(), requestFilters.getEndTime());
        }

        final TimeRange range = requestFilters != null && requestFilters.getTimeRange() != null
            ? requestFilters.getTimeRange()
            : this.filters.getTimeRange();

        final OffsetDateTime end = OffsetDateTime.now();
        return new TimeSpan(end.minus(range.getDuration()), end);
    }

    /**
     * Resolves the effective monitor filters from request filters.
     * Falls back to this watchlist's defaults if not specified in request.
     *
     * @param requestFilters the request filters (may be null)
     * @return the resolved monitor filters
     */
    public MonitorFilters resolveFilters(final MonitorFilters requestFilters) {
        final boolean onlyCritical = requestFilters != null && requestFilters.getOnlyCritical() != null
            ? requestFilters.getOnlyCritical()
            : this.filters.isOnlyCritical();

        final boolean sortBySeverity = requestFilters != null && requestFilters.getSortBySeverity() != null
            ? requestFilters.getSortBySeverity()
            : this.filters.isSortBySeverity();

        final boolean groupedView = requestFilters != null && requestFilters.getGroupedView() != null
            ? requestFilters.getGroupedView()
            : this.filters.isGroupedView();

        return MonitorFilters.resolved(onlyCritical, sortBySeverity, groupedView);
    }
}
