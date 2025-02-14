package org.jordijaspers.eventify.api.dashboard.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.dashboard.model.request.CreateDashboardRequest;
import org.jordijaspers.eventify.api.team.model.Team;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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
    private Set<DashboardCheck> dashboardChecks = new HashSet<>();

    /**
     * A constructor to create a new dashboard.
     *
     * @param request The request to create the dashboard.
     * @param email   The email of the user creating the dashboard.
     * @param team    The team the dashboard belongs to.
     */
    public Dashboard(final CreateDashboardRequest request, final String email, final Team team) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.global = request.isGlobal();
        this.team = team;

        this.updatedBy = email;
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
     * Clear the configuration of the dashboard.
     */
    public void clearConfiguration() {
        dashboardChecks.clear();
        groups.clear();
    }

    /**
     * Retrieve all ungrouped checks in the dashboard.
     *
     * @return The ungrouped checks.
     */
    public Set<Check> getUngroupedChecks() {
        return dashboardChecks.stream()
            .filter(dashboardChecks -> isNull(dashboardChecks.getGroup()))
            .map(DashboardCheck::getCheck)
            .collect(Collectors.toSet());
    }

    /**
     * Retrieve grouped checks in the dashboard. The checks are grouped by the group they belong to.
     *
     * @return The grouped checks.
     */
    public Map<DashboardGroup, Set<Check>> getGroupedChecks() {
        return dashboardChecks.stream()
            .filter(dashboardCheck -> nonNull(dashboardCheck.getGroup()))
            .collect(
                Collectors.groupingBy(
                    DashboardCheck::getGroup,
                    Collectors.mapping(DashboardCheck::getCheck, Collectors.toSet())
                )
            );
    }
}
