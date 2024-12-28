package org.jordijaspers.eventify.api.dashboard.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.dashboard.model.request.CreateDashboardRequest;
import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.user.model.User;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.*;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "dashboard")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Dashboard implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "global")
    private boolean global;

    @CreationTimestamp
    @Column(
        name = "created",
        updatable = false
    )
    private LocalDateTime created;

    @Column(
        name = "updated_by",
        updatable = false
    )
    private String updatedBy;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @OneToMany(
        mappedBy = "dashboard",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("displayOrder")
    private Set<DashboardGroup> groups = new HashSet<>();

    @OneToMany(
        mappedBy = "dashboard",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("group NULLS LAST, displayOrder")
    private List<DashboardCheck> dashboardChecks = new ArrayList<>();

    /**
     * A constructor to create a new dashboard.
     *
     * @param request The request to create the dashboard.
     * @param user    The user creating the dashboard.
     */
    public Dashboard(final CreateDashboardRequest request, final User user, final Team team) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.global = request.isGlobal();
        this.team = team;

        this.updatedBy = user.getUsername();
        this.lastUpdated = LocalDateTime.now();
        this.created = LocalDateTime.now();
    }

    /**
     * Add an existing check to the dashboard.
     *
     * @param check        The check to add.
     * @param group        The group to add the check to.
     * @param displayOrder The display order of the check.
     */
    public void addCheck(final Check check, final DashboardGroup group, final int displayOrder) {
        final DashboardCheck dashboardCheck = new DashboardCheck(this, check, group, displayOrder);
        dashboardChecks.add(dashboardCheck);
    }

    /**
     * Clear the dashboard configuration.
     */
    public void clearConfiguration() {
        dashboardChecks.clear();
        groups.clear();
    }
}
